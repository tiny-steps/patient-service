package com.tintsteps.patientservice.config; // Or your equivalent package

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${internal.api.secret}")
    private String internalApiSecret;

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder()
                // The key fix: Add the secret to the base builder
                .defaultHeader("X-Internal-Secret", internalApiSecret)
                // Add a filter to log outgoing request headers for debugging
                .filter(logRequestHeaders());
    }

    @Bean
    public WebClient publicWebClient(WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder.build();
    }

    /**
     * A WebClient for calling secure, token-protected endpoints.
     * It adds an ExchangeFilterFunction to propagate the JWT from the
     * current security context.
     */
    @Bean
    public WebClient secureWebClient(WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder
                .filter(jwtPropagationFilter())
                .build();
    }

    /**
     * Creates a filter that intercepts requests to add the Authorization header.
     * It retrieves the JWT from the reactive security context.
     * @return An ExchangeFilterFunction that adds a Bearer token.
     */
    private ExchangeFilterFunction jwtPropagationFilter() {
        return (request, next) -> ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> {
                    Authentication authentication = context.getAuthentication();
                    if (authentication!= null && authentication.getCredentials() instanceof String jwt) {
                        ClientRequest authorizedRequest = ClientRequest.from(request)
                                .headers(headers -> headers.setBearerAuth(jwt))
                                .build();
                        return next.exchange(authorizedRequest);
                    }
                    return next.exchange(request);
                })
                .switchIfEmpty(next.exchange(request));
    }

    /**
     * This logging filter will print the headers of every outgoing request
     * made by any WebClient created from the builder.
     */
    private ExchangeFilterFunction logRequestHeaders() {
        return (clientRequest, next) -> {
            logger.info("================ Outgoing Request from Patient-Service ================");
            logger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                    values.forEach(value -> {
                        String toLog = "X-Internal-Secret".equalsIgnoreCase(name) ? "[MASKED]" : value;
                        logger.info("Header: {}={}", name, toLog);
                    })
            );
            logger.info("======================================================================");
            return next.exchange(clientRequest);
        };
    }
}
