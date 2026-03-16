package com.shift_cut.Mapper;

import com.shift_cut.Model.Appointment;
import com.shift_cut.Model.DTO.AppointmentUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentUpdateMapper {

    @Mapping(target = "barber", expression = "java(com.shift_cut.Model.UserEntity.builder().id(dto.getBarberId()).build())")
    @Mapping(target = "user", expression = "java(com.shift_cut.Model.UserEntity.builder().id(dto.getUserId()).build())")
    void updateFromDto(AppointmentUpdateDTO dto, @MappingTarget Appointment entity);
}

