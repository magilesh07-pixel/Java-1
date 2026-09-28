package com.garagedesk.service;

import com.garagedesk.dto.request.CreateMechanicRequest;
import com.garagedesk.dto.response.MechanicResponse;
import com.garagedesk.entity.Mechanic;
import com.garagedesk.entity.enums.MechanicStatus;

import java.util.List;

public interface MechanicService {
    MechanicResponse createMechanic(CreateMechanicRequest request);
    MechanicResponse getMechanicById(Long id);
    List<MechanicResponse> getAllMechanics();
    List<MechanicResponse> getAvailableMechanics();
    Mechanic getMechanicEntity(Long id);
    void updateMechanicStatus(Long id, MechanicStatus status);
}
