package com.tintsteps.patientservice.integration.service;

import com.tintsteps.patientservice.dto.UserRegistrationRequest;
import com.tintsteps.patientservice.integration.model.UserModel;
import com.tintsteps.patientservice.model.ResponseModel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceIntegration {

    private final WebClient publicWebClient;
    private final io.github.resilience4j.retry.Retry authServiceRetry;
    private final io.github.resilience4j.circuitbreaker.CircuitBreaker authServiceCircuitBreaker;
    private final io.github.resilience4j.timelimiter.TimeLimiter authServiceTimeLimiter;
    private final WebClient secureWebClient;

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
                .bodyToMono(new ParameterizedTypeReference<ResponseModel<UserModel>>() {
                })
                .map(ResponseModel::getData)
                .transformDeferred(RetryOperator.of(authServiceRetry))
                .transformDeferred(CircuitBreakerOperator.of(authServiceCircuitBreaker))
                .transformDeferred(TimeLimiterOperator.of(authServiceTimeLimiter))
                .onErrorMap(throwable -> new AuthenticationServiceException("Auth service is unavailable", throwable));
    }

    // Fallback method
    public Mono<UserModel> registerUserFallback(UserRegistrationRequest request, Throwable t) {
        log.warn("Fallback for registerUser: {}", request.getEmail(), t);
        return Mono.error(new RuntimeException("User registration failed", t));
    }

    /**
     * Updates user email in auth service
     *
     * @param userId the user ID
     * @param email  the new email
     * @return success response
     */
    @CircuitBreaker(name = "ts-auth-service", fallbackMethod = "registerUserFallback")
    @Retry(name = "ts-auth-service")
    @TimeLimiter(name = "ts-auth-service")
    public Mono<Void> updateUserEmail(String userId, String email) {
        log.info("Updating user email in auth service for user ID: {} to email: {}", userId, email);

        return secureWebClient.patch()
                .uri(authServiceBaseUrl + "/api/auth/users/{userId}", userId)
                .bodyValue(new EmailUpdateRequest(email))
                .retrieve()
                .bodyToMono(Void.class)
                .transformDeferred(RetryOperator.of(authServiceRetry))
                .transformDeferred(CircuitBreakerOperator.of(authServiceCircuitBreaker))
                .transformDeferred(TimeLimiterOperator.of(authServiceTimeLimiter))
                .doOnSuccess(
                        result -> log.info("Successfully updated user email in auth service for user ID: {}", userId))
                .onErrorMap(throwable -> {
                    log.error("Failed to update user email in auth service for user ID: {}", userId, throwable);
                    return new AuthenticationServiceException("Failed to update user email in auth service", throwable);
                });
    }

    /**
     * Deletes a user from the auth service
     *
     * @param userId the user ID to delete
     * @return void - the delete operation completes successfully
     */
    @CircuitBreaker(name = "ts-auth-service", fallbackMethod = "registerUserFallback")
    @Retry(name = "ts-auth-service")
    @TimeLimiter(name = "ts-auth-service")
    public Mono<Void> deleteUser(String userId) {
        log.info("Deleting user from auth service with user ID: {}", userId);

        return secureWebClient.delete()
                .uri(authServiceBaseUrl + "/api/auth/users/{userId}", userId)
                .retrieve()
                .bodyToMono(Void.class)
                .transformDeferred(RetryOperator.of(authServiceRetry))
                .transformDeferred(CircuitBreakerOperator.of(authServiceCircuitBreaker))
                .transformDeferred(TimeLimiterOperator.of(authServiceTimeLimiter))
                .doOnSuccess(result -> log.info("Successfully deleted user from auth service with user ID: {}", userId))
                .onErrorMap(throwable -> {
                    log.error("Failed to delete user from auth service with user ID: {}", userId, throwable);
                    return new AuthenticationServiceException("Failed to delete user from auth service", throwable);
                });
    }

    /**
     * Request model for updating user email
     */
    private record EmailUpdateRequest(String email) {
    }
}
