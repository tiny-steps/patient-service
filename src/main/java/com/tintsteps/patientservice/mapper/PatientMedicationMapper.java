package com.tintsteps.patientservice.mapper;

import com.tintsteps.patientservice.dto.PatientMedicationDto;
import com.tintsteps.patientservice.model.PatientMedication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PatientMedicationMapper {

    PatientMedicationMapper INSTANCE = Mappers.getMapper(PatientMedicationMapper.class);

    @Mapping(source = "patient.id", target = "patientId")
    PatientMedicationDto patientMedicationToPatientMedicationDto(PatientMedication patientMedication);

    @Mapping(source = "patientId", target = "patient.id")
    PatientMedication patientMedicationDtoToPatientMedication(PatientMedicationDto patientMedicationDto);
}
