package com.tintsteps.patientservice.integration;

import com.tintsteps.patientservice.integration.dto.AddressDto;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceIntegration {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.address-service.base-url:http://ts-address-service}")
    private String addressServiceBaseUrl;

    @CircuitBreaker(name = "ts-address-service", fallbackMethod = "getAddressFallback")
    @Retry(name = "ts-address-service")
    @TimeLimiter(name = "ts-address-service")
    public CompletableFuture<AddressDto> getAddressById(UUID addressId) {
        log.info("Fetching address by ID: {}", addressId);
        
        return webClientBuilder.build()
                .get()
                .uri(addressServiceBaseUrl + "/api/v1/addresses/{id}", addressId)
                .retrieve()
                .bodyToMono(AddressDto.class)
                .timeout(Duration.ofSeconds(5))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-address-service", fallbackMethod = "getAddressesByUserIdFallback")
    @Retry(name = "ts-address-service")
    @TimeLimiter(name = "ts-address-service")
    public CompletableFuture<List<AddressDto>> getAddressesByUserId(String userId) {
        log.info("Fetching addresses for user ID: {}", userId);
        
        return webClientBuilder.build()
                .get()
                .uri(addressServiceBaseUrl + "/api/v1/addresses/user/{userId}", userId)
                .retrieve()
                .bodyToFlux(AddressDto.class)
                .collectList()
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-address-service", fallbackMethod = "getAddressesByUserIdAndTypeFallback")
    @Retry(name = "ts-address-service")
    @TimeLimiter(name = "ts-address-service")
    public CompletableFuture<List<AddressDto>> getAddressesByUserIdAndType(String userId, String type) {
        log.info("Fetching addresses for user ID: {} and type: {}", userId, type);
        
        return webClientBuilder.build()
                .get()
                .uri(addressServiceBaseUrl + "/api/v1/addresses/user/{userId}/type/{type}", userId, type)
                .retrieve()
                .bodyToFlux(AddressDto.class)
                .collectList()
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-address-service", fallbackMethod = "createAddressFallback")
    @Retry(name = "ts-address-service")
    @TimeLimiter(name = "ts-address-service")
    public CompletableFuture<AddressDto> createAddress(AddressDto addressDto) {
        log.info("Creating address for user ID: {}", addressDto.getUserId());
        
        return webClientBuilder.build()
                .post()
                .uri(addressServiceBaseUrl + "/api/v1/addresses")
                .bodyValue(addressDto)
                .retrieve()
                .bodyToMono(AddressDto.class)
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-address-service", fallbackMethod = "updateAddressFallback")
    @Retry(name = "ts-address-service")
    @TimeLimiter(name = "ts-address-service")
    public CompletableFuture<AddressDto> updateAddress(UUID addressId, AddressDto addressDto) {
        log.info("Updating address ID: {}", addressId);
        
        return webClientBuilder.build()
                .put()
                .uri(addressServiceBaseUrl + "/api/v1/addresses/{id}", addressId)
                .bodyValue(addressDto)
                .retrieve()
                .bodyToMono(AddressDto.class)
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-address-service", fallbackMethod = "deleteAddressFallback")
    @Retry(name = "ts-address-service")
    @TimeLimiter(name = "ts-address-service")
    public CompletableFuture<Void> deleteAddress(UUID addressId) {
        log.info("Deleting address ID: {}", addressId);
        
        return webClientBuilder.build()
                .delete()
                .uri(addressServiceBaseUrl + "/api/v1/addresses/{id}", addressId)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-address-service", fallbackMethod = "validateAddressFallback")
    @Retry(name = "ts-address-service")
    @TimeLimiter(name = "ts-address-service")
    public CompletableFuture<Boolean> validateAddress(UUID addressId) {
        log.info("Validating address: {}", addressId);
        
        return webClientBuilder.build()
                .get()
                .uri(addressServiceBaseUrl + "/api/v1/addresses/{id}/validate", addressId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofSeconds(3))
                .toFuture();
    }

    // Fallback methods
    public CompletableFuture<AddressDto> getAddressFallback(UUID addressId, Exception ex) {
        log.warn("Address service fallback triggered for getAddressById: {}, error: {}", addressId, ex.getMessage());
        AddressDto fallbackAddress = new AddressDto();
        fallbackAddress.setId(addressId);
        fallbackAddress.setStreet("Unknown Address");
        fallbackAddress.setCity("Unknown City");
        fallbackAddress.setCountry("Unknown Country");
        return CompletableFuture.completedFuture(fallbackAddress);
    }

    public CompletableFuture<List<AddressDto>> getAddressesByUserIdFallback(String userId, Exception ex) {
        log.warn("Address service fallback triggered for getAddressesByUserId: {}, error: {}", userId, ex.getMessage());
        return CompletableFuture.completedFuture(List.of());
    }

    public CompletableFuture<List<AddressDto>> getAddressesByUserIdAndTypeFallback(String userId, String type, Exception ex) {
        log.warn("Address service fallback triggered for getAddressesByUserIdAndType: {}, {}, error: {}", userId, type, ex.getMessage());
        return CompletableFuture.completedFuture(List.of());
    }

    public CompletableFuture<AddressDto> createAddressFallback(AddressDto addressDto, Exception ex) {
        log.warn("Address service fallback triggered for createAddress, error: {}", ex.getMessage());
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<AddressDto> updateAddressFallback(UUID addressId, AddressDto addressDto, Exception ex) {
        log.warn("Address service fallback triggered for updateAddress: {}, error: {}", addressId, ex.getMessage());
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> deleteAddressFallback(UUID addressId, Exception ex) {
        log.warn("Address service fallback triggered for deleteAddress: {}, error: {}", addressId, ex.getMessage());
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Boolean> validateAddressFallback(UUID addressId, Exception ex) {
        log.warn("Address service fallback triggered for validateAddress: {}, error: {}", addressId, ex.getMessage());
        return CompletableFuture.completedFuture(false);
    }

    // Synchronous methods for backward compatibility
    public AddressDto getAddressByIdSync(UUID addressId) {
        try {
            return getAddressById(addressId).get();
        } catch (Exception e) {
            log.error("Error fetching address synchronously: {}", e.getMessage());
            return getAddressFallback(addressId, e).join();
        }
    }

    public List<AddressDto> getAddressesByUserIdSync(String userId) {
        try {
            return getAddressesByUserId(userId).get();
        } catch (Exception e) {
            log.error("Error fetching addresses synchronously: {}", e.getMessage());
            return List.of();
        }
    }

    public Boolean validateAddressSync(UUID addressId) {
        try {
            return validateAddress(addressId).get();
        } catch (Exception e) {
            log.error("Error validating address synchronously: {}", e.getMessage());
            return false;
        }
    }
}
