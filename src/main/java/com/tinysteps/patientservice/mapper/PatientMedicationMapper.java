package com.tinysteps.patientservice.mapper;

import com.tinysteps.patientservice.dto.PatientMedicationDto;
import com.tinysteps.patientservice.model.PatientMedication;
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
