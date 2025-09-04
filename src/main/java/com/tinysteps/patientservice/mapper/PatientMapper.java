package com.tinysteps.patientservice.mapper;

import com.tinysteps.patientservice.dto.PatientDto;
import com.tinysteps.patientservice.model.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PatientMapper {

    PatientMapper INSTANCE = Mappers.getMapper(PatientMapper.class);

    PatientDto patientToPatientDto(Patient patient);

    Patient patientDtoToPatient(PatientDto patientDto);
}
