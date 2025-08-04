package com.tintsteps.patientservice.mapper;

import com.tintsteps.patientservice.dto.PatientInsuranceDto;
import com.tintsteps.patientservice.model.PatientInsurance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PatientInsuranceMapper {

    PatientInsuranceMapper INSTANCE = Mappers.getMapper(PatientInsuranceMapper.class);

    @Mapping(source = "patient.id", target = "patientId")
    PatientInsuranceDto patientInsuranceToPatientInsuranceDto(PatientInsurance patientInsurance);

    @Mapping(source = "patientId", target = "patient.id")
    PatientInsurance patientInsuranceDtoToPatientInsurance(PatientInsuranceDto patientInsuranceDto);
}
