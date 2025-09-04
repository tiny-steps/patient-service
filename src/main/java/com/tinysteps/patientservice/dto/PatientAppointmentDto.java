package com.tinysteps.patientservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PatientAppointmentDto {
    private UUID id;
    private UUID patientId;
    private UUID appointmentId;
}
