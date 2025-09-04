package com.tinysteps.patientservice.mapper;

import com.tinysteps.patientservice.dto.PatientAppointmentDto;
import com.tinysteps.patientservice.model.PatientAppointment;
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
