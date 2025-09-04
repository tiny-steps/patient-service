package com.tinysteps.patientservice.mapper;

import com.tinysteps.patientservice.dto.PatientEmergencyContactDto;
import com.tinysteps.patientservice.model.PatientEmergencyContact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PatientEmergencyContactMapper {

    PatientEmergencyContactMapper INSTANCE = Mappers.getMapper(PatientEmergencyContactMapper.class);

    @Mapping(source = "patient.id", target = "patientId")
    PatientEmergencyContactDto patientEmergencyContactToPatientEmergencyContactDto(PatientEmergencyContact patientEmergencyContact);

    @Mapping(source = "patientId", target = "patient.id")
    PatientEmergencyContact patientEmergencyContactDtoToPatientEmergencyContact(PatientEmergencyContactDto patientEmergencyContactDto);
}
