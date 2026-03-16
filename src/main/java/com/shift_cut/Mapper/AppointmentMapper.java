package com.shift_cut.Mapper;

import com.shift_cut.Model.Appointment;
import com.shift_cut.Model.DTO.AppointmentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "barberId", source = "barber.id")
    @Mapping(target = "barberUsername", source = "barber.username")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userUsername", source = "user.username")
    AppointmentDTO toDto(Appointment appointment);

    // si en el futuro se necesita mapear DTO->Entity se puede agregar con mappings inversos
}

