package com.tintsteps.patientservice.integration.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentDto {
    private UUID id;
    private UUID patientId;
    private UUID doctorId;
    private UUID sessionTypeId;
    private LocalDateTime scheduledDateTime;
    private Integer durationMinutes;
    private String status; // SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    private String notes;
    private String meetingLink;
    private String meetingId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
