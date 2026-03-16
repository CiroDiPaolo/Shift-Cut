package com.shift_cut.Mapper;

import com.shift_cut.Model.Appointment;
import com.shift_cut.Model.DTO.AppointmentCreateDTO;
import com.shift_cut.Model.UserEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

class AppointmentCreateMapperTest {

    private final AppointmentCreateMapper mapper = Mappers.getMapper(AppointmentCreateMapper.class);

    @Test
    void toEntity_mapsFieldsAndCreatesUserEntitiesWithIds() {
        AppointmentCreateDTO dto = AppointmentCreateDTO.builder()
                .typeShift(com.shift_cut.Model.Enum.ServiceType.HAIR_CUT)
                .date(LocalDate.of(2026, 7, 1))
                .time(LocalTime.of(9, 0))
                .barberId(10L)
                .userId(20L)
                .build();

        Appointment entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getTypeShift()).isEqualTo(dto.getTypeShift());
        assertThat(entity.getDate()).isEqualTo(dto.getDate());
        assertThat(entity.getTime()).isEqualTo(dto.getTime());
        UserEntity barber = entity.getBarber();
        UserEntity user = entity.getUser();
        assertThat(barber).isNotNull();
        assertThat(user).isNotNull();
        assertThat(barber.getId()).isEqualTo(10L);
        assertThat(user.getId()).isEqualTo(20L);
    }
}

