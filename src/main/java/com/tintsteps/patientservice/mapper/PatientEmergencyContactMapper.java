package com.tintsteps.patientservice.mapper;

import com.tintsteps.patientservice.dto.PatientEmergencyContactDto;
import com.tintsteps.patientservice.model.PatientEmergencyContact;
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
