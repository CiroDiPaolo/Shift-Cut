package com.shift_cut.Mapper;

import com.shift_cut.Model.Appointment;
import com.shift_cut.Model.DTO.AppointmentUpdateDTO;
import com.shift_cut.Model.UserEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

class AppointmentUpdateMapperTest {

    private final AppointmentUpdateMapper mapper = Mappers.getMapper(AppointmentUpdateMapper.class);

    @Test
    void updateFromDto_appliesChangesAndSetsUserEntities() {
        Appointment existing = Appointment.builder()
                .id(1L)
                .typeShift(com.shift_cut.Model.Enum.ServiceType.HAIR_CUT)
                .date(LocalDate.of(2026,4,10))
                .time(LocalTime.of(10,30))
                .barber(UserEntity.builder().id(1L).build())
                .user(UserEntity.builder().id(2L).build())
                .build();

        AppointmentUpdateDTO dto = new AppointmentUpdateDTO(
                com.shift_cut.Model.Enum.ServiceType.HAIR_CUT_AND_BEARD,
                LocalDate.of(2026,5,1),
                LocalTime.of(14,0),
                10L,
                20L
        );

        mapper.updateFromDto(dto, existing);

        assertThat(existing.getTypeShift()).isEqualTo(dto.typeShift());
        assertThat(existing.getDate()).isEqualTo(dto.date());
        assertThat(existing.getTime()).isEqualTo(dto.time());
        assertThat(existing.getBarber()).isNotNull();
        assertThat(existing.getUser()).isNotNull();
        assertThat(existing.getBarber().getId()).isEqualTo(10L);
        assertThat(existing.getUser().getId()).isEqualTo(20L);
    }
}

