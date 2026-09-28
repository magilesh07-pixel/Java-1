package com.garagedesk.service.impl;

import com.garagedesk.dto.request.CreateBayRequest;
import com.garagedesk.dto.response.BayResponse;
import com.garagedesk.entity.Bay;
import com.garagedesk.entity.JobCard;
import com.garagedesk.entity.enums.BayStatus;
import com.garagedesk.exception.BadRequestException;
import com.garagedesk.exception.ResourceNotFoundException;
import com.garagedesk.repository.BayRepository;
import com.garagedesk.repository.JobCardRepository;
import com.garagedesk.service.AuditLogService;
import com.garagedesk.service.BayService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BayServiceImpl implements BayService {

    private final BayRepository bayRepository;
    private final JobCardRepository jobCardRepository;
    private final AuditLogService auditLogService;

    public BayServiceImpl(BayRepository bayRepository,
                          @Lazy JobCardRepository jobCardRepository,
                          AuditLogService auditLogService) {
        this.bayRepository = bayRepository;
        this.jobCardRepository = jobCardRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public BayResponse createBay(CreateBayRequest request) {
        String bayNum = request.getBayNumber().trim();
        if (bayRepository.existsByBayNumber(bayNum)) {
            throw new BadRequestException("Bay with number '" + bayNum + "' already exists.");
        }

        Bay bay = new Bay(bayNum, request.getBayType(), request.getStatus());
        Bay saved = bayRepository.save(bay);
        auditLogService.log("Bay", saved.getId(), "CREATE", "SYSTEM", "Created service bay " + saved.getBayNumber());
        return BayResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BayResponse getBayById(Long id) {
        Bay bay = getBayEntity(id);
        Optional<JobCard> activeJob = jobCardRepository.findActiveJobByBayId(id);
        return BayResponse.fromEntity(bay, activeJob.map(JobCard::getId).orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BayResponse> getAllBays() {
        return bayRepository.findAll().stream()
                .map(bay -> {
                    Optional<JobCard> activeJob = jobCardRepository.findActiveJobByBayId(bay.getId());
                    return BayResponse.fromEntity(bay, activeJob.map(JobCard::getId).orElse(null));
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BayResponse> getAvailableBays() {
        return bayRepository.findByStatus(BayStatus.AVAILABLE).stream()
                .map(BayResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Bay getBayEntity(Long id) {
        return bayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bay not found with ID: " + id));
    }

    @Override
    @Transactional
    public void updateBayStatus(Long bayId, BayStatus status) {
        Bay bay = getBayEntity(bayId);
        bay.setStatus(status);
        bayRepository.save(bay);
        auditLogService.log("Bay", bay.getId(), "STATUS_CHANGE", "SYSTEM", "Bay status updated to " + status);
    }
}
