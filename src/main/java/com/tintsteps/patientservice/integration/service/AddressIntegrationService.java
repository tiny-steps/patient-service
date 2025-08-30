package com.tintsteps.patientservice.integration.service;

import com.tintsteps.patientservice.exception.IntegrationException;
import com.tintsteps.patientservice.integration.model.AddressIntegrationModel;
import com.tintsteps.patientservice.integration.model.IntegrationResponseModel;
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
 * Service for integrating with Address Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddressIntegrationService {

    private final WebClient publicWebClient;
    private final Retry addressServiceRetry;
    private final CircuitBreaker addressServiceCircuitBreaker;
    private final TimeLimiter addressServiceTimeLimiter;

    @Value("${integration.address-service.base-url:http://ts-address-service/api/v1/addresses}")
    private String addressServiceBaseUrl;

    /**
     * Validates if an address exists
     *
     * @param addressId the address ID to validate
     * @return true if address exists
     * @throws IntegrationException if integration fails
     */
    public boolean validateAddress(UUID addressId) {
        try {
            log.debug("Validating address with ID: {}", addressId);

            AddressIntegrationModel address = getAddressById(addressId).block();
            boolean exists = address != null;

            log.debug("Address validation result for ID {}: exists={}", addressId, exists);
            return exists;

        } catch (Exception e) {
            log.error("Failed to validate address with ID: {}", addressId, e);
            throw new IntegrationException("Address Service",
                "Failed to validate address: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves address information by ID
     *
     * @param addressId the address ID
     * @return address information
     * @throws IntegrationException if address not found or integration fails
     */
    public Mono<AddressIntegrationModel> getAddressById(UUID addressId) {
        log.debug("Fetching address information for ID: {}", addressId);

        return publicWebClient.get()
                .uri(addressServiceBaseUrl + "/{id}", addressId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<IntegrationResponseModel<AddressIntegrationModel>>() {})
                .map(IntegrationResponseModel::data)
                .transformDeferred(RetryOperator.of(addressServiceRetry))
                .transformDeferred(CircuitBreakerOperator.of(addressServiceCircuitBreaker))
                .transformDeferred(TimeLimiterOperator.of(addressServiceTimeLimiter))
                .doOnSuccess(address -> log.debug("Successfully fetched address information for ID: {}", addressId))
                .onErrorMap(throwable -> {
                    log.error("Failed to fetch address with ID: {}", addressId, throwable);
                    return new IntegrationException("Address Service",
                        "Failed to fetch address information: " + throwable.getMessage(), throwable);
                });
    }

    /**
     * Validates if an address is suitable for medical practice
     *
     * @param addressId the address ID to validate
     * @return true if address is suitable for practice
     */
    public boolean validatePracticeAddress(UUID addressId) {
        try {
            AddressIntegrationModel address = getAddressById(addressId).block();

            if (address == null) {
                return false;
            }

            // Check if address type is suitable for medical practice
            String addressType = address.type();
            boolean isSuitable = "CLINIC".equalsIgnoreCase(addressType) ||
                               "HOSPITAL".equalsIgnoreCase(addressType);

            log.debug("Practice address validation for ID {}: type={}, suitable={}",
                     addressId, addressType, isSuitable);

            return isSuitable;

        } catch (Exception e) {
            log.error("Failed to validate practice address with ID: {}", addressId, e);
            return false;
        }
    }

    /**
     * Checks if an address exists
     *
     * @param addressId the address ID to check
     * @return true if address exists
     */
    public boolean addressExists(UUID addressId) {
        try {
            AddressIntegrationModel address = getAddressById(addressId).block();
            return address != null;
        } catch (IntegrationException e) {
            log.debug("Address with ID {} does not exist", addressId);
            return false;
        }
    }
}
