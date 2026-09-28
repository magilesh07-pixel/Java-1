package com.garagedesk.service.impl;

import com.garagedesk.dto.request.AddServiceItemRequest;
import com.garagedesk.dto.request.AssignJobCardRequest;
import com.garagedesk.dto.request.CreateJobCardRequest;
import com.garagedesk.dto.request.UpdateJobStatusRequest;
import com.garagedesk.dto.response.JobCardResponse;
import com.garagedesk.entity.*;
import com.garagedesk.entity.enums.BayStatus;
import com.garagedesk.entity.enums.JobStatus;
import com.garagedesk.entity.enums.MechanicStatus;
import com.garagedesk.exception.BadRequestException;
import com.garagedesk.exception.BayConflictException;
import com.garagedesk.exception.InvalidStatusTransitionException;
import com.garagedesk.exception.ResourceNotFoundException;
import com.garagedesk.repository.*;
import com.garagedesk.service.AuditLogService;
import com.garagedesk.service.JobCardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobCardServiceImpl implements JobCardService {

    private final JobCardRepository jobCardRepository;
    private final VehicleRepository vehicleRepository;
    private final BayRepository bayRepository;
    private final MechanicRepository mechanicRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final AuditLogService auditLogService;

    public JobCardServiceImpl(JobCardRepository jobCardRepository,
                              VehicleRepository vehicleRepository,
                              BayRepository bayRepository,
                              MechanicRepository mechanicRepository,
                              ServiceItemRepository serviceItemRepository,
                              AuditLogService auditLogService) {
        this.jobCardRepository = jobCardRepository;
        this.vehicleRepository = vehicleRepository;
        this.bayRepository = bayRepository;
        this.mechanicRepository = mechanicRepository;
        this.serviceItemRepository = serviceItemRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public JobCardResponse createJobCard(CreateJobCardRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + request.getVehicleId()));

        JobCard jobCard = new JobCard(vehicle, request.getRequestedServices(), request.getCustomerComplaints());

        if (request.getInitialItems() != null && !request.getInitialItems().isEmpty()) {
            for (AddServiceItemRequest itemReq : request.getInitialItems()) {
                ServiceItem item = new ServiceItem(
                        jobCard,
                        itemReq.getItemName(),
                        itemReq.getItemType(),
                        itemReq.getQuantity(),
                        itemReq.getUnitPrice()
                );
                jobCard.addServiceItem(item);
            }
        }

        JobCard saved = jobCardRepository.save(jobCard);
        auditLogService.log("JobCard", saved.getId(), "CREATE", "SERVICE_ADVISOR",
                "Created job card for vehicle " + vehicle.getRegistrationNumber() + " with requested services: " + saved.getRequestedServices());

        return JobCardResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public JobCardResponse assignJobCard(Long id, AssignJobCardRequest request) {
        JobCard jobCard = getJobCardEntity(id);

        if (jobCard.getStatus() == JobStatus.COMPLETED || jobCard.getStatus() == JobStatus.CANCELLED) {
            throw new BadRequestException("Cannot assign bay or mechanic to a job card that is " + jobCard.getStatus());
        }

        Bay newBay = bayRepository.findById(request.getBayId())
                .orElseThrow(() -> new ResourceNotFoundException("Bay not found with ID: " + request.getBayId()));

        if (newBay.getStatus() == BayStatus.UNDER_MAINTENANCE) {
            throw new BayConflictException("Bay '" + newBay.getBayNumber() + "' is currently UNDER MAINTENANCE and cannot accept jobs.");
        }

        // Business Rule 1 Enforced: Prevent assigning a bay that is already occupied by an unfinished job.
        Optional<JobCard> activeJob = jobCardRepository.findActiveJobByBayId(newBay.getId());
        if (activeJob.isPresent() && !activeJob.get().getId().equals(jobCard.getId())) {
            JobCard occupyingJob = activeJob.get();
            throw new BayConflictException(String.format(
                    "Bay Conflict: Bay '%s' is already occupied by active Job Card #%d (%s) for Vehicle '%s'. A bay can hold only one active job card at a time.",
                    newBay.getBayNumber(),
                    occupyingJob.getId(),
                    occupyingJob.getStatus(),
                    occupyingJob.getVehicle().getRegistrationNumber()
            ));
        }

        // If the job card was already in a different bay, release the previous bay
        if (jobCard.getBay() != null && !jobCard.getBay().getId().equals(newBay.getId())) {
            Bay previousBay = jobCard.getBay();
            previousBay.setStatus(BayStatus.AVAILABLE);
            bayRepository.save(previousBay);
        }

        // Assign new bay and mark occupied
        jobCard.setBay(newBay);
        newBay.setStatus(BayStatus.OCCUPIED);
        bayRepository.save(newBay);

        // Assign mechanic if provided
        if (request.getMechanicId() != null) {
            Mechanic mechanic = mechanicRepository.findById(request.getMechanicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mechanic not found with ID: " + request.getMechanicId()));

            // Release old mechanic if different
            if (jobCard.getMechanic() != null && !jobCard.getMechanic().getId().equals(mechanic.getId())) {
                Mechanic oldMechanic = jobCard.getMechanic();
                oldMechanic.setStatus(MechanicStatus.AVAILABLE);
                mechanicRepository.save(oldMechanic);
            }

            jobCard.setMechanic(mechanic);
            mechanic.setStatus(MechanicStatus.BUSY);
            mechanicRepository.save(mechanic);
        }

        // Advance status from WAITING to IN_PROGRESS when bay is assigned
        if (jobCard.getStatus() == JobStatus.WAITING) {
            jobCard.setStatus(JobStatus.IN_PROGRESS);
        }

        JobCard saved = jobCardRepository.save(jobCard);
        auditLogService.log("JobCard", saved.getId(), "ASSIGN", "SERVICE_ADVISOR",
                String.format("Assigned Job Card #%d to Bay '%s' and Mechanic '%s'",
                        saved.getId(),
                        newBay.getBayNumber(),
                        saved.getMechanic() != null ? saved.getMechanic().getName() : "Unassigned"));

        return JobCardResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public JobCardResponse updateJobStatus(Long id, UpdateJobStatusRequest request) {
        JobCard jobCard = getJobCardEntity(id);
        JobStatus currentStatus = jobCard.getStatus();
        JobStatus targetStatus = request.getStatus();

        if (currentStatus == targetStatus) {
            return JobCardResponse.fromEntity(jobCard);
        }

        if (currentStatus == JobStatus.COMPLETED) {
            throw new InvalidStatusTransitionException("Job Card #" + id + " is already COMPLETED and cannot be modified.");
        }

        if (currentStatus == JobStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Job Card #" + id + " is CANCELLED and cannot be modified.");
        }

        // Business Rule 2 Enforced: A job card cannot move to 'completed' status until it has passed 'quality-check'.
        if (targetStatus == JobStatus.COMPLETED) {
            if (currentStatus != JobStatus.QUALITY_CHECK) {
                throw new InvalidStatusTransitionException(String.format(
                        "Business Rule Violation: Job Card #%d cannot move directly from '%s' to 'COMPLETED'. It must first pass 'QUALITY_CHECK'.",
                        id, currentStatus
                ));
            }
            jobCard.setCompletedAt(LocalDateTime.now());

            // Release bay when job is completed
            if (jobCard.getBay() != null) {
                Bay bay = jobCard.getBay();
                bay.setStatus(BayStatus.AVAILABLE);
                bayRepository.save(bay);
            }

            // Release mechanic when job is completed
            if (jobCard.getMechanic() != null) {
                Mechanic mechanic = jobCard.getMechanic();
                mechanic.setStatus(MechanicStatus.AVAILABLE);
                mechanicRepository.save(mechanic);
            }
        }

        if (targetStatus == JobStatus.CANCELLED) {
            // Free bay & mechanic on cancel
            if (jobCard.getBay() != null) {
                Bay bay = jobCard.getBay();
                bay.setStatus(BayStatus.AVAILABLE);
                bayRepository.save(bay);
            }
            if (jobCard.getMechanic() != null) {
                Mechanic mechanic = jobCard.getMechanic();
                mechanic.setStatus(MechanicStatus.AVAILABLE);
                mechanicRepository.save(mechanic);
            }
        }

        if (targetStatus == JobStatus.QUALITY_CHECK && request.getQualityCheckNotes() != null) {
            jobCard.setQualityCheckNotes(request.getQualityCheckNotes());
        }

        jobCard.setStatus(targetStatus);
        JobCard saved = jobCardRepository.save(jobCard);

        auditLogService.log("JobCard", saved.getId(), "STATUS_CHANGE", "OPERATOR",
                String.format("Job Card #%d transitioned from %s to %s", id, currentStatus, targetStatus));

        return JobCardResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public JobCardResponse addServiceItem(Long id, AddServiceItemRequest request) {
        JobCard jobCard = getJobCardEntity(id);

        if (jobCard.getStatus() == JobStatus.COMPLETED || jobCard.getStatus() == JobStatus.CANCELLED) {
            throw new BadRequestException("Cannot add service or parts to a " + jobCard.getStatus() + " job card.");
        }

        ServiceItem item = new ServiceItem(
                jobCard,
                request.getItemName(),
                request.getItemType(),
                request.getQuantity(),
                request.getUnitPrice()
        );
        jobCard.addServiceItem(item);
        serviceItemRepository.save(item);

        // If a bill was already generated, recalculate bill totals
        if (jobCard.getBill() != null) {
            Bill bill = jobCard.getBill();
            recalculateBillAmounts(bill, jobCard);
        }

        JobCard saved = jobCardRepository.save(jobCard);
        auditLogService.log("JobCard", saved.getId(), "ADD_ITEM", "MECHANIC",
                String.format("Added %s '%s' (Qty: %d, Unit Price: %s)",
                        request.getItemType(), request.getItemName(), request.getQuantity(), request.getUnitPrice()));

        return JobCardResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public JobCardResponse removeServiceItem(Long jobCardId, Long serviceItemId) {
        JobCard jobCard = getJobCardEntity(jobCardId);

        if (jobCard.getStatus() == JobStatus.COMPLETED) {
            throw new BadRequestException("Cannot remove items from a completed job card.");
        }

        ServiceItem item = serviceItemRepository.findById(serviceItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Service item not found with ID: " + serviceItemId));

        if (!item.getJobCard().getId().equals(jobCardId)) {
            throw new BadRequestException("Service item does not belong to Job Card #" + jobCardId);
        }

        jobCard.removeServiceItem(item);
        serviceItemRepository.delete(item);

        if (jobCard.getBill() != null) {
            recalculateBillAmounts(jobCard.getBill(), jobCard);
        }

        JobCard saved = jobCardRepository.save(jobCard);
        auditLogService.log("JobCard", saved.getId(), "REMOVE_ITEM", "OPERATOR", "Removed item #" + serviceItemId);
        return JobCardResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public JobCardResponse getJobCardById(Long id) {
        return JobCardResponse.fromEntity(getJobCardEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobCardResponse> getAllJobCards() {
        return jobCardRepository.findAll().stream()
                .map(JobCardResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobCardResponse> getJobCardsByStatus(JobStatus status) {
        return jobCardRepository.findByStatus(status).stream()
                .map(JobCardResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobCardResponse> getJobCardsByVehicle(Long vehicleId) {
        return jobCardRepository.findByVehicleId(vehicleId).stream()
                .map(JobCardResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JobCard getJobCardEntity(Long id) {
        return jobCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job Card not found with ID: " + id));
    }

    private void recalculateBillAmounts(Bill bill, JobCard jobCard) {
        java.math.BigDecimal parts = java.math.BigDecimal.ZERO;
        java.math.BigDecimal labour = java.math.BigDecimal.ZERO;

        for (ServiceItem item : jobCard.getServiceItems()) {
            if (item.getItemType() == com.garagedesk.entity.enums.ItemType.PART) {
                parts = parts.add(item.getTotalPrice());
            } else {
                labour = labour.add(item.getTotalPrice());
            }
        }

        bill.setPartsTotal(parts);
        bill.setLabourCharges(labour);
        bill.calculateTotal();
    }
}
