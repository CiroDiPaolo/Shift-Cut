package com.shift_cut.Service;

import com.shift_cut.Exceptions.AppointmentNotFound;
import com.shift_cut.Model.Appointment;
import com.shift_cut.Model.DTO.AppointmentDTO;
import com.shift_cut.Repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository repo;

    private AppointmentDTO toDTO(Appointment a) {
        return AppointmentDTO.builder()
                .id(a.getId())
                .typeShift(a.getTypeShift())
                .date(a.getDate())
                .time(a.getTime())
                .barberId(a.getBarber().getId())
                .barberUsername(a.getBarber().getUsername())
                .userId(a.getUser().getId())
                .userUsername(a.getUser().getUsername())
                .build();
    }

    public AppointmentDTO getAppointmentById(Long id) {
        Appointment appointment = repo.findById(id)
                .orElseThrow(() -> new AppointmentNotFound("Appointment not found with id: " + id));
        return toDTO(appointment);
    }

    public List<AppointmentDTO> getAllAppointments() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    public List<AppointmentDTO> getAppointmentsByUserId(Long userId) {
        return repo.findByUser_Id(userId).stream().map(this::toDTO).toList();
    }

    public AppointmentDTO createAppointment(Appointment appointment) {
        return toDTO(repo.save(appointment));
    }

    public AppointmentDTO updateAppointment(Long id, Appointment appointment) {
        if (!repo.existsById(id)) {
            throw new AppointmentNotFound("Appointment not found with id: " + id);
        }
        appointment.setId(id);
        return toDTO(repo.save(appointment));
    }

    public void deleteAppointment(Long id) {
        if (!repo.existsById(id)) {
            throw new AppointmentNotFound("Appointment not found with id: " + id);
        }
        repo.deleteById(id);
    }
}
