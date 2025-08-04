package com.tintsteps.patientservice.mapper;

import com.tintsteps.patientservice.dto.PatientDto;
import com.tintsteps.patientservice.model.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PatientMapper {

    PatientMapper INSTANCE = Mappers.getMapper(PatientMapper.class);

    PatientDto patientToPatientDto(Patient patient);

    Patient patientDtoToPatient(PatientDto patientDto);
}
