package com.garagedesk.service;

import com.garagedesk.dto.request.CreateVehicleRequest;
import com.garagedesk.dto.response.VehicleResponse;
import com.garagedesk.entity.Vehicle;

import java.util.List;

public interface VehicleService {
    VehicleResponse registerVehicle(CreateVehicleRequest request);
    VehicleResponse getVehicleById(Long id);
    VehicleResponse getVehicleByRegistration(String registrationNumber);
    List<VehicleResponse> getAllVehicles();
    Vehicle getVehicleEntity(Long id);
}
