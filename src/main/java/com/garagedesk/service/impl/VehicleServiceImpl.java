package com.garagedesk.service.impl;

import com.garagedesk.dto.request.CreateVehicleRequest;
import com.garagedesk.dto.response.VehicleResponse;
import com.garagedesk.entity.Vehicle;
import com.garagedesk.exception.BadRequestException;
import com.garagedesk.exception.ResourceNotFoundException;
import com.garagedesk.repository.VehicleRepository;
import com.garagedesk.service.AuditLogService;
import com.garagedesk.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final AuditLogService auditLogService;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, AuditLogService auditLogService) {
        this.vehicleRepository = vehicleRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public VehicleResponse registerVehicle(CreateVehicleRequest request) {
        String regNum = request.getRegistrationNumber().trim().toUpperCase();
        if (vehicleRepository.existsByRegistrationNumber(regNum)) {
            throw new BadRequestException("Vehicle with registration number '" + regNum + "' already exists.");
        }

        Vehicle vehicle = new Vehicle(
                regNum,
                request.getBrand(),
                request.getModel(),
                request.getManufacturingYear(),
                request.getOwnerName(),
                request.getOwnerPhone(),
                request.getOwnerEmail()
        );

        Vehicle saved = vehicleRepository.save(vehicle);
        auditLogService.log("Vehicle", saved.getId(), "REGISTER", "SYSTEM", "Registered vehicle " + saved.getRegistrationNumber());
        return VehicleResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        return VehicleResponse.fromEntity(getVehicleEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleByRegistration(String registrationNumber) {
        Vehicle vehicle = vehicleRepository.findByRegistrationNumber(registrationNumber.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with registration number: " + registrationNumber));
        return VehicleResponse.fromEntity(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Vehicle getVehicleEntity(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));
    }
}
