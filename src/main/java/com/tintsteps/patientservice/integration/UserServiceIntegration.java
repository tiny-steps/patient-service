package com.tintsteps.patientservice.integration;

import com.tintsteps.patientservice.integration.dto.UserDto;
import com.tintsteps.patientservice.integration.model.UserUpdateRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceIntegration {

    private final WebClient.Builder webClientBuilder;
    private final WebClient secureWebClient;
    private final io.github.resilience4j.retry.Retry userServiceRetry;
    private final io.github.resilience4j.circuitbreaker.CircuitBreaker userServiceCircuitBreaker;
    private final io.github.resilience4j.timelimiter.TimeLimiter userServiceTimeLimiter;

    @Value("${services.user-service.base-url:http://ts-user-service}")
    private String userServiceBaseUrl;

    @Value("${integration.user-service.base-url:http://ts-user-service/api/v1/users}")
    private String userServiceApiUrl;

    @CircuitBreaker(name = "ts-user-service", fallbackMethod = "getUserFallback")
    @Retry(name = "ts-user-service")
    @TimeLimiter(name = "ts-user-service")
    public CompletableFuture<UserDto> getUserById(UUID userId) {
        log.info("Fetching user by ID: {}", userId);

        return webClientBuilder.build()
                .get()
                .uri(userServiceBaseUrl + "/api/v1/users/{id}", userId)
                .retrieve()
                .bodyToMono(UserDto.class)
                .timeout(Duration.ofSeconds(5))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-user-service", fallbackMethod = "getUserByEmailFallback")
    @Retry(name = "ts-user-service")
    @TimeLimiter(name = "ts-user-service")
    public CompletableFuture<UserDto> getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);

        return webClientBuilder.build()
                .get()
                .uri(userServiceBaseUrl + "/api/v1/users/email/{email}", email)
                .retrieve()
                .bodyToMono(UserDto.class)
                .timeout(Duration.ofSeconds(5))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-user-service", fallbackMethod = "validateUserFallback")
    @Retry(name = "ts-user-service")
    @TimeLimiter(name = "ts-user-service")
    public CompletableFuture<Boolean> validateUser(UUID userId) {
        log.info("Validating user: {}", userId);

        return webClientBuilder.build()
                .get()
                .uri(userServiceBaseUrl + "/api/v1/users/{id}/validate", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofSeconds(3))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-user-service", fallbackMethod = "getUsersByRoleFallback")
    @Retry(name = "ts-user-service")
    @TimeLimiter(name = "ts-user-service")
    public CompletableFuture<List<UserDto>> getUsersByRole(String role) {
        log.info("Fetching users by role: {}", role);

        return webClientBuilder.build()
                .get()
                .uri(userServiceBaseUrl + "/api/v1/users/role/{role}", role)
                .retrieve()
                .bodyToFlux(UserDto.class)
                .collectList()
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }

    /**
     * Retrieves user information by ID (reactive version)
     *
     * @param userId the user ID
     * @return user information
     */
    public Mono<UserDto> getUserByIdReactive(UUID userId) {
        log.debug("Fetching user information for ID: {}", userId);

        return secureWebClient.get()
                .uri(userServiceApiUrl + "/{id}", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseModel<UserDto>>() {
                })
                .map(ResponseModel::data)
                .transformDeferred(RetryOperator.of(userServiceRetry))
                .transformDeferred(CircuitBreakerOperator.of(userServiceCircuitBreaker))
                .transformDeferred(TimeLimiterOperator.of(userServiceTimeLimiter))
                .doOnSuccess(user -> log.debug("Successfully fetched user information for ID: {}", userId))
                .onErrorMap(throwable -> {
                    log.error("Failed to fetch user with ID: {}", userId, throwable);
                    return new IntegrationException("User Service",
                            "Failed to fetch user information: " + throwable.getMessage(), throwable);
                });
    }

    /**
     * Updates user information
     *
     * @param userId            the user ID to update
     * @param userUpdateRequest the user update request
     * @return void - the update operation completes successfully
     */
    public Mono<Void> updateUser(UUID userId, UserUpdateRequest userUpdateRequest) {
        log.info("Updating user information for ID: {} with request: {}", userId, userUpdateRequest);

        return secureWebClient.patch()
                .uri(userServiceApiUrl + "/{id}", userId)
                .bodyValue(userUpdateRequest)
                .retrieve()
                .bodyToMono(Void.class)
                .transformDeferred(RetryOperator.of(userServiceRetry))
                .transformDeferred(CircuitBreakerOperator.of(userServiceCircuitBreaker))
                .transformDeferred(TimeLimiterOperator.of(userServiceTimeLimiter))
                .doOnSuccess(result -> log.info("Successfully updated user information for ID: {}", userId))
                .onErrorMap(throwable -> {
                    log.error("Failed to update user with ID: {}", userId, throwable);
                    return new IntegrationException("User Service",
                            "Failed to update user information: " + throwable.getMessage(), throwable);
                });
    }

    /**
     * Deletes a user from the user service
     *
     * @param userId the user ID to delete
     * @return void - the delete operation completes successfully
     */
    public Mono<Void> deleteUser(UUID userId) {
        log.info("Deleting user from user service with user ID: {}", userId);

        return secureWebClient.delete()
                .uri(userServiceApiUrl + "/{id}", userId)
                .retrieve()
                .bodyToMono(Void.class)
                .transformDeferred(RetryOperator.of(userServiceRetry))
                .transformDeferred(CircuitBreakerOperator.of(userServiceCircuitBreaker))
                .transformDeferred(TimeLimiterOperator.of(userServiceTimeLimiter))
                .doOnSuccess(result -> log.info("Successfully deleted user from user service with user ID: {}", userId))
                .onErrorMap(throwable -> {
                    log.error("Failed to delete user from user service with user ID: {}", userId, throwable);
                    return new IntegrationException("User Service",
                            "Failed to delete user: " + throwable.getMessage(), throwable);
                });
    }

    // Fallback methods
    public CompletableFuture<UserDto> getUserFallback(UUID userId, Exception ex) {
        log.warn("User service fallback triggered for getUserById: {}, error: {}", userId, ex.getMessage());
        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(userId);
        fallbackUser.setEmail("unknown@example.com");
        fallbackUser.setFirstName("Unknown");
        fallbackUser.setLastName("User");
        return CompletableFuture.completedFuture(fallbackUser);
    }

    public CompletableFuture<UserDto> getUserByEmailFallback(String email, Exception ex) {
        log.warn("User service fallback triggered for getUserByEmail: {}, error: {}", email, ex.getMessage());
        UserDto fallbackUser = new UserDto();
        fallbackUser.setEmail(email);
        fallbackUser.setFirstName("Unknown");
        fallbackUser.setLastName("User");
        return CompletableFuture.completedFuture(fallbackUser);
    }

    public CompletableFuture<Boolean> validateUserFallback(UUID userId, Exception ex) {
        log.warn("User service fallback triggered for validateUser: {}, error: {}", userId, ex.getMessage());
        return CompletableFuture.completedFuture(false);
    }

    public CompletableFuture<List<UserDto>> getUsersByRoleFallback(String role, Exception ex) {
        log.warn("User service fallback triggered for getUsersByRole: {}, error: {}", role, ex.getMessage());
        return CompletableFuture.completedFuture(List.of());
    }

    // Synchronous methods for backward compatibility
    public UserDto getUserByIdSync(UUID userId) {
        try {
            return getUserById(userId).get();
        } catch (Exception e) {
            log.error("Error fetching user synchronously: {}", e.getMessage());
            return getUserFallback(userId, e).join();
        }
    }

    public Boolean validateUserSync(UUID userId) {
        try {
            return validateUser(userId).get();
        } catch (Exception e) {
            log.error("Error validating user synchronously: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Exception class for User Service integration failures
     */
    public static class IntegrationException extends RuntimeException {
        public IntegrationException(String service, String message, Throwable cause) {
            super(service + ": " + message, cause);
        }
    }

    /**
     * Response model for user service
     */
    private record ResponseModel<T>(T data) {
    }
}
