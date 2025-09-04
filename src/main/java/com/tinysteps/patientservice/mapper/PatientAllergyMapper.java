package com.tinysteps.patientservice.mapper;

import com.tinysteps.patientservice.dto.PatientAllergyDto;
import com.tinysteps.patientservice.model.PatientAllergy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PatientAllergyMapper {

    PatientAllergyMapper INSTANCE = Mappers.getMapper(PatientAllergyMapper.class);

    @Mapping(source = "patient.id", target = "patientId")
    PatientAllergyDto patientAllergyToPatientAllergyDto(PatientAllergy patientAllergy);

    @Mapping(source = "patientId", target = "patient.id")
    PatientAllergy patientAllergyDtoToPatientAllergy(PatientAllergyDto patientAllergyDto);
}
