package com.shift_cut.Service;

import com.shift_cut.Exceptions.AppointmentNotFound;
import com.shift_cut.Exceptions.UserIsNotBarberException;
import com.shift_cut.Exceptions.UserNotFound;
import com.shift_cut.Mapper.AppointmentCreateMapper;
import com.shift_cut.Mapper.AppointmentMapper;
import com.shift_cut.Mapper.AppointmentUpdateMapper;
import com.shift_cut.Model.Appointment;
import com.shift_cut.Model.DTO.AppointmentCreateDTO;
import com.shift_cut.Model.DTO.AppointmentDTO;
import com.shift_cut.Model.DTO.AppointmentUpdateDTO;
import com.shift_cut.Model.UserEntity;
import com.shift_cut.Repository.AppointmentRepository;
import com.shift_cut.Repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository repo;
    private final AppointmentMapper mapper;
    private final AppointmentCreateMapper createMapper;
    private final AppointmentUpdateMapper updateMapper;
    private final UserEntityRepository userRepo;

    public AppointmentDTO getAppointmentById(Long id) {
        Appointment appointment = repo.findById(id)
                .orElseThrow(() -> new AppointmentNotFound("Appointment not found with id: " + id));
        return mapper.toDto(appointment);
    }

    public List<AppointmentDTO> getAllAppointments() {
        return repo.findAll().stream().map(mapper::toDto).toList();
    }

    public List<AppointmentDTO> getAppointmentsByUserId(Long userId) {
        return repo.findByUser_Id(userId).stream().map(mapper::toDto).toList();
    }

    public AppointmentDTO createAppointment(Appointment appointment) {
        return mapper.toDto(repo.save(appointment));
    }

    public AppointmentDTO createAppointment(AppointmentCreateDTO dto) {
        // Primero validar existencia de barber y user y el rol del barber
        UserEntity user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new UserNotFound("User not found with id: " + dto.userId()));
        UserEntity barber = userRepo.findById(dto.barberId())
                .orElseThrow(() -> new UserNotFound("User not found with id: " + dto.barberId()));

        // Validar rol de barber antes de construir la entidad
        if (barber.getRole() != com.shift_cut.Model.Enum.Role.BARBER) {
            throw new UserIsNotBarberException("User with id: " + barber.getId() + " is not a barber");
        }

        // Intentar mapear con MapStruct; si devuelve null, construir manualmente la entidad (fallback seguro)
        Appointment appointment = createMapper.toEntity(dto);
        if (appointment == null) {
            appointment = Appointment.builder()
                    .typeShift(dto.typeShift())
                    .date(dto.date())
                    .time(dto.time())
                    .build();
        }

        // Asignar entidades gestionadas
        appointment.setUser(user);
        appointment.setBarber(barber);

        return mapper.toDto(repo.save(appointment));
    }

    public AppointmentDTO updateAppointment(Long id, Appointment appointment) {
        if (!repo.existsById(id)) {
            throw new AppointmentNotFound("Appointment not found with id: " + id);
        }
        appointment.setId(id);
        return mapper.toDto(repo.save(appointment));
    }

    public AppointmentDTO updateAppointment(Long id, AppointmentUpdateDTO dto) {
        Appointment existing = repo.findById(id)
                .orElseThrow(() -> new AppointmentNotFound("Appointment not found with id: " + id));
        // aplicar cambios sobre la entidad existente
        updateMapper.updateFromDto(dto, existing);
        // Validar existencia de barber y user y asignar las entidades gestionadas
        UserEntity user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new UserNotFound("User not found with id: " + dto.userId()));
        UserEntity barber = userRepo.findById(dto.barberId())
                .orElseThrow(() -> new UserNotFound("User not found with id: " + dto.barberId()));

        // Validar rol de barber
        if (barber.getRole() != com.shift_cut.Model.Enum.Role.BARBER) {
            throw new com.shift_cut.Exceptions.UserIsNotBarberException("User with id: " + barber.getId() + " is not a barber");
        }
        existing.setUser(user);
        existing.setBarber(barber);

        return mapper.toDto(repo.save(existing));
    }

    public void deleteAppointment(Long id) {
        if (!repo.existsById(id)) {
            throw new AppointmentNotFound("Appointment not found with id: " + id);
        }
        repo.deleteById(id);
    }
}
