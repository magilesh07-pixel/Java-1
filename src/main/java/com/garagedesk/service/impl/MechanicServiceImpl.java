package com.garagedesk.service.impl;

import com.garagedesk.dto.request.CreateMechanicRequest;
import com.garagedesk.dto.response.MechanicResponse;
import com.garagedesk.entity.Mechanic;
import com.garagedesk.entity.enums.MechanicStatus;
import com.garagedesk.exception.ResourceNotFoundException;
import com.garagedesk.repository.MechanicRepository;
import com.garagedesk.service.AuditLogService;
import com.garagedesk.service.MechanicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MechanicServiceImpl implements MechanicService {

    private final MechanicRepository mechanicRepository;
    private final AuditLogService auditLogService;

    public MechanicServiceImpl(MechanicRepository mechanicRepository, AuditLogService auditLogService) {
        this.mechanicRepository = mechanicRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public MechanicResponse createMechanic(CreateMechanicRequest request) {
        Mechanic mechanic = new Mechanic(
                request.getName(),
                request.getSpecialization(),
                request.getPhone(),
                request.getHourlyRate(),
                request.getStatus()
        );
        Mechanic saved = mechanicRepository.save(mechanic);
        auditLogService.log("Mechanic", saved.getId(), "CREATE", "SYSTEM", "Added mechanic " + saved.getName());
        return MechanicResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MechanicResponse getMechanicById(Long id) {
        return MechanicResponse.fromEntity(getMechanicEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MechanicResponse> getAllMechanics() {
        return mechanicRepository.findAll().stream()
                .map(MechanicResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MechanicResponse> getAvailableMechanics() {
        return mechanicRepository.findByStatus(MechanicStatus.AVAILABLE).stream()
                .map(MechanicResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Mechanic getMechanicEntity(Long id) {
        return mechanicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic not found with ID: " + id));
    }

    @Override
    @Transactional
    public void updateMechanicStatus(Long id, MechanicStatus status) {
        Mechanic mechanic = getMechanicEntity(id);
        mechanic.setStatus(status);
        mechanicRepository.save(mechanic);
        auditLogService.log("Mechanic", mechanic.getId(), "STATUS_CHANGE", "SYSTEM", "Mechanic status updated to " + status);
    }
}
