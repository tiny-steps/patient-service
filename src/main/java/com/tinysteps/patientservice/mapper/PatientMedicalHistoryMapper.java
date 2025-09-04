package com.tinysteps.patientservice.mapper;

import com.tinysteps.patientservice.dto.PatientMedicalHistoryDto;
import com.tinysteps.patientservice.model.PatientMedicalHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PatientMedicalHistoryMapper {

    PatientMedicalHistoryMapper INSTANCE = Mappers.getMapper(PatientMedicalHistoryMapper.class);

    @Mapping(source = "patient.id", target = "patientId")
    PatientMedicalHistoryDto patientMedicalHistoryToPatientMedicalHistoryDto(PatientMedicalHistory patientMedicalHistory);

    @Mapping(source = "patientId", target = "patient.id")
    PatientMedicalHistory patientMedicalHistoryDtoToPatientMedicalHistory(PatientMedicalHistoryDto patientMedicalHistoryDto);
}
