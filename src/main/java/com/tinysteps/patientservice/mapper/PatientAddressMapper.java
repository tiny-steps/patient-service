package com.tinysteps.patientservice.mapper;

import com.tinysteps.patientservice.dto.PatientAddressDto;
import com.tinysteps.patientservice.model.PatientAddress;
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
