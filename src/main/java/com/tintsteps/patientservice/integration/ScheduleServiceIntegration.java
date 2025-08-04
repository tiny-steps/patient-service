package com.tintsteps.patientservice.integration;

import com.tintsteps.patientservice.integration.dto.AppointmentDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceIntegration {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.schedule-service.base-url:http://ts-schedule-service}")
    private String scheduleServiceBaseUrl;

    @CircuitBreaker(name = "ts-schedule-service", fallbackMethod = "getAppointmentFallback")
    @Retry(name = "ts-schedule-service")
    @TimeLimiter(name = "ts-schedule-service")
    public CompletableFuture<AppointmentDto> getAppointmentById(UUID appointmentId) {
        log.info("Fetching appointment by ID: {}", appointmentId);
        
        return webClientBuilder.build()
                .get()
                .uri(scheduleServiceBaseUrl + "/api/v1/appointments/{id}", appointmentId)
                .retrieve()
                .bodyToMono(AppointmentDto.class)
                .timeout(Duration.ofSeconds(5))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-schedule-service", fallbackMethod = "getAppointmentsByPatientIdFallback")
    @Retry(name = "ts-schedule-service")
    @TimeLimiter(name = "ts-schedule-service")
    public CompletableFuture<List<AppointmentDto>> getAppointmentsByPatientId(UUID patientId) {
        log.info("Fetching appointments for patient ID: {}", patientId);
        
        return webClientBuilder.build()
                .get()
                .uri(scheduleServiceBaseUrl + "/api/v1/appointments/patient/{patientId}", patientId)
                .retrieve()
                .bodyToFlux(AppointmentDto.class)
                .collectList()
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-schedule-service", fallbackMethod = "getUpcomingAppointmentsFallback")
    @Retry(name = "ts-schedule-service")
    @TimeLimiter(name = "ts-schedule-service")
    public CompletableFuture<List<AppointmentDto>> getUpcomingAppointments(UUID patientId) {
        log.info("Fetching upcoming appointments for patient ID: {}", patientId);
        
        return webClientBuilder.build()
                .get()
                .uri(scheduleServiceBaseUrl + "/api/v1/appointments/patient/{patientId}/upcoming", patientId)
                .retrieve()
                .bodyToFlux(AppointmentDto.class)
                .collectList()
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-schedule-service", fallbackMethod = "getPastAppointmentsFallback")
    @Retry(name = "ts-schedule-service")
    @TimeLimiter(name = "ts-schedule-service")
    public CompletableFuture<List<AppointmentDto>> getPastAppointments(UUID patientId) {
        log.info("Fetching past appointments for patient ID: {}", patientId);
        
        return webClientBuilder.build()
                .get()
                .uri(scheduleServiceBaseUrl + "/api/v1/appointments/patient/{patientId}/past", patientId)
                .retrieve()
                .bodyToFlux(AppointmentDto.class)
                .collectList()
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-schedule-service", fallbackMethod = "getAppointmentsByStatusFallback")
    @Retry(name = "ts-schedule-service")
    @TimeLimiter(name = "ts-schedule-service")
    public CompletableFuture<List<AppointmentDto>> getAppointmentsByStatus(UUID patientId, String status) {
        log.info("Fetching appointments for patient ID: {} with status: {}", patientId, status);
        
        return webClientBuilder.build()
                .get()
                .uri(scheduleServiceBaseUrl + "/api/v1/appointments/patient/{patientId}/status/{status}", patientId, status)
                .retrieve()
                .bodyToFlux(AppointmentDto.class)
                .collectList()
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-schedule-service", fallbackMethod = "getAppointmentsByDateRangeFallback")
    @Retry(name = "ts-schedule-service")
    @TimeLimiter(name = "ts-schedule-service")
    public CompletableFuture<List<AppointmentDto>> getAppointmentsByDateRange(UUID patientId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching appointments for patient ID: {} between {} and {}", patientId, startDate, endDate);
        
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(scheduleServiceBaseUrl + "/api/v1/appointments/patient/{patientId}/date-range")
                        .queryParam("startDate", startDate.toString())
                        .queryParam("endDate", endDate.toString())
                        .build(patientId))
                .retrieve()
                .bodyToFlux(AppointmentDto.class)
                .collectList()
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-schedule-service", fallbackMethod = "createAppointmentFallback")
    @Retry(name = "ts-schedule-service")
    @TimeLimiter(name = "ts-schedule-service")
    public CompletableFuture<AppointmentDto> createAppointment(AppointmentDto appointmentDto) {
        log.info("Creating appointment for patient ID: {}", appointmentDto.getPatientId());
        
        return webClientBuilder.build()
                .post()
                .uri(scheduleServiceBaseUrl + "/api/v1/appointments")
                .bodyValue(appointmentDto)
                .retrieve()
                .bodyToMono(AppointmentDto.class)
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-schedule-service", fallbackMethod = "updateAppointmentFallback")
    @Retry(name = "ts-schedule-service")
    @TimeLimiter(name = "ts-schedule-service")
    public CompletableFuture<AppointmentDto> updateAppointment(UUID appointmentId, AppointmentDto appointmentDto) {
        log.info("Updating appointment ID: {}", appointmentId);
        
        return webClientBuilder.build()
                .put()
                .uri(scheduleServiceBaseUrl + "/api/v1/appointments/{id}", appointmentId)
                .bodyValue(appointmentDto)
                .retrieve()
                .bodyToMono(AppointmentDto.class)
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-schedule-service", fallbackMethod = "cancelAppointmentFallback")
    @Retry(name = "ts-schedule-service")
    @TimeLimiter(name = "ts-schedule-service")
    public CompletableFuture<AppointmentDto> cancelAppointment(UUID appointmentId) {
        log.info("Cancelling appointment ID: {}", appointmentId);
        
        return webClientBuilder.build()
                .patch()
                .uri(scheduleServiceBaseUrl + "/api/v1/appointments/{id}/cancel", appointmentId)
                .retrieve()
                .bodyToMono(AppointmentDto.class)
                .timeout(Duration.ofSeconds(5))
                .toFuture();
    }

    @CircuitBreaker(name = "ts-schedule-service", fallbackMethod = "validateAppointmentFallback")
    @Retry(name = "ts-schedule-service")
    @TimeLimiter(name = "ts-schedule-service")
    public CompletableFuture<Boolean> validateAppointment(UUID appointmentId) {
        log.info("Validating appointment: {}", appointmentId);
        
        return webClientBuilder.build()
                .get()
                .uri(scheduleServiceBaseUrl + "/api/v1/appointments/{id}/validate", appointmentId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofSeconds(3))
                .toFuture();
    }

    // Fallback methods
    public CompletableFuture<AppointmentDto> getAppointmentFallback(UUID appointmentId, Exception ex) {
        log.warn("Schedule service fallback triggered for getAppointmentById: {}, error: {}", appointmentId, ex.getMessage());
        AppointmentDto fallbackAppointment = new AppointmentDto();
        fallbackAppointment.setId(appointmentId);
        fallbackAppointment.setStatus("UNKNOWN");
        return CompletableFuture.completedFuture(fallbackAppointment);
    }

    public CompletableFuture<List<AppointmentDto>> getAppointmentsByPatientIdFallback(UUID patientId, Exception ex) {
        log.warn("Schedule service fallback triggered for getAppointmentsByPatientId: {}, error: {}", patientId, ex.getMessage());
        return CompletableFuture.completedFuture(List.of());
    }

    public CompletableFuture<List<AppointmentDto>> getUpcomingAppointmentsFallback(UUID patientId, Exception ex) {
        log.warn("Schedule service fallback triggered for getUpcomingAppointments: {}, error: {}", patientId, ex.getMessage());
        return CompletableFuture.completedFuture(List.of());
    }

    public CompletableFuture<List<AppointmentDto>> getPastAppointmentsFallback(UUID patientId, Exception ex) {
        log.warn("Schedule service fallback triggered for getPastAppointments: {}, error: {}", patientId, ex.getMessage());
        return CompletableFuture.completedFuture(List.of());
    }

    public CompletableFuture<List<AppointmentDto>> getAppointmentsByStatusFallback(UUID patientId, String status, Exception ex) {
        log.warn("Schedule service fallback triggered for getAppointmentsByStatus: {}, {}, error: {}", patientId, status, ex.getMessage());
        return CompletableFuture.completedFuture(List.of());
    }

    public CompletableFuture<List<AppointmentDto>> getAppointmentsByDateRangeFallback(UUID patientId, LocalDateTime startDate, LocalDateTime endDate, Exception ex) {
        log.warn("Schedule service fallback triggered for getAppointmentsByDateRange: {}, error: {}", patientId, ex.getMessage());
        return CompletableFuture.completedFuture(List.of());
    }

    public CompletableFuture<AppointmentDto> createAppointmentFallback(AppointmentDto appointmentDto, Exception ex) {
        log.warn("Schedule service fallback triggered for createAppointment, error: {}", ex.getMessage());
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<AppointmentDto> updateAppointmentFallback(UUID appointmentId, AppointmentDto appointmentDto, Exception ex) {
        log.warn("Schedule service fallback triggered for updateAppointment: {}, error: {}", appointmentId, ex.getMessage());
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<AppointmentDto> cancelAppointmentFallback(UUID appointmentId, Exception ex) {
        log.warn("Schedule service fallback triggered for cancelAppointment: {}, error: {}", appointmentId, ex.getMessage());
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Boolean> validateAppointmentFallback(UUID appointmentId, Exception ex) {
        log.warn("Schedule service fallback triggered for validateAppointment: {}, error: {}", appointmentId, ex.getMessage());
        return CompletableFuture.completedFuture(false);
    }

    // Synchronous methods for backward compatibility
    public AppointmentDto getAppointmentByIdSync(UUID appointmentId) {
        try {
            return getAppointmentById(appointmentId).get();
        } catch (Exception e) {
            log.error("Error fetching appointment synchronously: {}", e.getMessage());
            return getAppointmentFallback(appointmentId, e).join();
        }
    }

    public List<AppointmentDto> getAppointmentsByPatientIdSync(UUID patientId) {
        try {
            return getAppointmentsByPatientId(patientId).get();
        } catch (Exception e) {
            log.error("Error fetching appointments synchronously: {}", e.getMessage());
            return List.of();
        }
    }

    public Boolean validateAppointmentSync(UUID appointmentId) {
        try {
            return validateAppointment(appointmentId).get();
        } catch (Exception e) {
            log.error("Error validating appointment synchronously: {}", e.getMessage());
            return false;
        }
    }
}
