package com.tintsteps.patientservice.mapper;

import com.tintsteps.patientservice.dto.PatientAddressDto;
import com.tintsteps.patientservice.model.PatientAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PatientAddressMapper {

    PatientAddressMapper INSTANCE = Mappers.getMapper(PatientAddressMapper.class);

    @Mapping(source = "patient.id", target = "patientId")
    PatientAddressDto patientAddressToPatientAddressDto(PatientAddress patientAddress);

    @Mapping(source = "patientId", target = "patient.id")
    PatientAddress patientAddressDtoToPatientAddress(PatientAddressDto patientAddressDto);
}
