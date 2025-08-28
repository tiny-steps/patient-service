package com.tintsteps.patientservice.integration;

import com.tintsteps.patientservice.dto.UserRegistrationRequest;
import com.tintsteps.patientservice.integration.model.UserModel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceIntegration {

    private final WebClient publicWebClient;

    @Value("${services.auth-service.base-url:http://ts-auth-service}")
    private String authServiceBaseUrl;

    @CircuitBreaker(name = "ts-auth-service", fallbackMethod = "registerUserFallback")
    @Retry(name = "ts-auth-service")
    @TimeLimiter(name = "ts-auth-service")
    public Mono<UserModel> registerUser(UserRegistrationRequest registrationRequest) {
        log.info("Registering user via auth-service with email: {}", registrationRequest.getEmail());

        return publicWebClient.post()
                .uri(authServiceBaseUrl + "/api/auth/register")
                .bodyValue(registrationRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<AuthServiceResponseModel<UserModel>>() {
                })
                .map(AuthServiceResponseModel::data)
                .timeout(Duration.ofSeconds(10));
    }

    // Fallback method
    public Mono<UserModel> registerUserFallback(UserRegistrationRequest registrationRequest, Throwable t) {
        log.error("Auth service fallback triggered for registerUser: {}, error: {}", registrationRequest.getEmail(),
                t.getMessage());
        return Mono.error(new RuntimeException("User registration failed - auth service unavailable", t));
    }
}
