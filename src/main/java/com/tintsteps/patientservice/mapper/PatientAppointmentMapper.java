package com.tintsteps.patientservice.mapper;

import com.tintsteps.patientservice.dto.PatientAppointmentDto;
import com.tintsteps.patientservice.model.PatientAppointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PatientAppointmentMapper {

    PatientAppointmentMapper INSTANCE = Mappers.getMapper(PatientAppointmentMapper.class);

    @Mapping(source = "patient.id", target = "patientId")
    PatientAppointmentDto patientAppointmentToPatientAppointmentDto(PatientAppointment patientAppointment);

    @Mapping(source = "patientId", target = "patient.id")
    PatientAppointment patientAppointmentDtoToPatientAppointment(PatientAppointmentDto patientAppointmentDto);
}
