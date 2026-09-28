package com.garagedesk.service;

import com.garagedesk.dto.request.CreateBayRequest;
import com.garagedesk.dto.response.BayResponse;
import com.garagedesk.entity.Bay;

import java.util.List;

public interface BayService {
    BayResponse createBay(CreateBayRequest request);
    BayResponse getBayById(Long id);
    List<BayResponse> getAllBays();
    List<BayResponse> getAvailableBays();
    Bay getBayEntity(Long id);
    void updateBayStatus(Long bayId, com.garagedesk.entity.enums.BayStatus status);
}
