package com.tintsteps.patientservice.config;

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

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    /**
     * A WebClient for calling public, unsecured endpoints.
     * It uses the load-balanced builder but adds no authentication.
     */
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
}
