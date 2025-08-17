package com.tintsteps.patientservice.integration;

import com.tintsteps.patientservice.dto.UserRegistrationRequest;
import com.tintsteps.patientservice.dto.UserRegistrationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceIntegration {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.auth-service.base-url:http://ts-auth-service}")
    private String authServiceBaseUrl;

    @CircuitBreaker(name = "ts-auth-service", fallbackMethod = "registerUserFallback")
    @Retry(name = "ts-auth-service")
    @TimeLimiter(name = "ts-auth-service")
    public CompletableFuture<UserRegistrationResponse> registerUser(UserRegistrationRequest registrationRequest) {
        log.info("Registering user via auth-service with email: {}", registrationRequest.getEmail());

        return webClientBuilder.build()
                .post()
                .uri(authServiceBaseUrl + "/api/v1/auth/register")
                .body(Mono.just(registrationRequest), UserRegistrationRequest.class)
                .retrieve()
                .bodyToMono(UserRegistrationResponse.class)
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }

    // Fallback method
    public CompletableFuture<UserRegistrationResponse> registerUserFallback(UserRegistrationRequest registrationRequest, Exception ex) {
        log.error("Auth service fallback triggered for registerUser: {}, error: {}", registrationRequest.getEmail(), ex.getMessage());
        throw new RuntimeException("User registration failed - auth service unavailable", ex);
    }
}
