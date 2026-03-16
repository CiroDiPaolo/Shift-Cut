package com.shift_cut.Mapper;

import com.shift_cut.Model.Appointment;
import com.shift_cut.Model.UserEntity;
import com.shift_cut.Model.DTO.AppointmentCreateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentCreateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "barber", expression = "java(com.shift_cut.Model.UserEntity.builder().id(dto.getBarberId()).build())")
    @Mapping(target = "user", expression = "java(com.shift_cut.Model.UserEntity.builder().id(dto.getUserId()).build())")
    Appointment toEntity(AppointmentCreateDTO dto);
}

