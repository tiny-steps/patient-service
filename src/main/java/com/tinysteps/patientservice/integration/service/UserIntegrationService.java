package com.tinysteps.patientservice.integration.service;

import com.tinysteps.patientservice.exception.IntegrationException;
import com.tinysteps.patientservice.integration.model.IntegrationResponseModel;
import com.tinysteps.patientservice.integration.model.UserIntegrationModel;
import com.tinysteps.patientservice.integration.model.UserUpdateRequest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for integrating with User Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserIntegrationService {

    private final WebClient secureWebClient;
    private final Retry userServiceRetry;
    private final CircuitBreaker userServiceCircuitBreaker;
    private final TimeLimiter userServiceTimeLimiter;

    @Value("${integration.user-service.base-url:http://ts-user-service/api/v1/users}")
    private String userServiceBaseUrl;

    /**
     * Validates if a user exists and has DOCTOR role
     *
     * @param userId the user ID to validate
     * @return true if user exists and is a patient
     * @throws IntegrationException if integration fails
     */
    public boolean validateDoctorUser(UUID userId) {
        try {
            log.debug("Validating patient user with ID: {}", userId);

            UserIntegrationModel user = getUserById(userId).block();
            boolean isDoctor = user != null && "DOCTOR".equalsIgnoreCase(user.role());

            log.debug("User validation result for ID {}: exists={}, isDoctor={}",
                    userId, user != null, isDoctor);

            return isDoctor;

        } catch (Exception e) {
            log.error("Failed to validate patient user with ID: {}", userId, e);
            throw new IntegrationException("User Service",
                    "Failed to validate patient user: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves user information by ID
     *
     * @param userId the user ID
     * @return user information
     * @throws IntegrationException if user not found or integration fails
     */
    public Mono<UserIntegrationModel> getUserById(UUID userId) {
        log.debug("Fetching user information for ID: {}", userId);

        return secureWebClient.get()
                .uri(userServiceBaseUrl + "/{id}", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<IntegrationResponseModel<UserIntegrationModel>>() {
                })
                .map(IntegrationResponseModel::data)
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
     * Checks if a user exists
     *
     * @param userId the user ID to check
     * @return true if user exists
     */
    public boolean userExists(UUID userId) {
        try {
            UserIntegrationModel user = getUserById(userId).block();
            return user != null;
        } catch (IntegrationException e) {
            log.debug("User with ID {} does not exist", userId);
            return false;
        }
    }

    /**
     * Updates user information
     *
     * @param userId            the user ID to update
     * @param userUpdateRequest the user update request
     * @return void - the update operation completes successfully
     * @throws IntegrationException if update fails
     */
    public Mono<Void> updateUser(UUID userId, UserUpdateRequest userUpdateRequest) {
        log.info("Updating user information for ID: {} with request: {}", userId, userUpdateRequest);

        return secureWebClient.patch()
                .uri(userServiceBaseUrl + "/{id}", userId)
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
     * @throws IntegrationException if delete fails
     */
    public Mono<Void> deleteUser(UUID userId) {
        log.info("Deleting user from user service with user ID: {}", userId);

        return secureWebClient.delete()
                .uri(userServiceBaseUrl + "/{id}", userId)
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

    /**
     * Exception class for User Service integration failures
     */
    public static class UserServiceUnavailableException extends RuntimeException {
        public UserServiceUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
