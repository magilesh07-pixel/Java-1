package com.garagedesk.dto.response;

import com.garagedesk.entity.JobCard;
import com.garagedesk.entity.enums.JobStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JobCardResponse {

    private Long id;
    private VehicleResponse vehicle;
    private BayResponse bay;
    private MechanicResponse mechanic;
    private JobStatus status;
    private String requestedServices;
    private String customerComplaints;
    private String qualityCheckNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private List<ServiceItemResponse> serviceItems = new ArrayList<>();
    private BillResponse bill;

    public JobCardResponse() {
    }

    public static JobCardResponse fromEntity(JobCard job) {
        if (job == null) return null;
        JobCardResponse response = new JobCardResponse();
        response.setId(job.getId());
        response.setVehicle(VehicleResponse.fromEntity(job.getVehicle()));
        response.setBay(BayResponse.fromEntity(job.getBay()));
        response.setMechanic(MechanicResponse.fromEntity(job.getMechanic()));
        response.setStatus(job.getStatus());
        response.setRequestedServices(job.getRequestedServices());
        response.setCustomerComplaints(job.getCustomerComplaints());
        response.setQualityCheckNotes(job.getQualityCheckNotes());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        response.setCompletedAt(job.getCompletedAt());

        if (job.getServiceItems() != null) {
            response.setServiceItems(
                    job.getServiceItems().stream()
                            .map(ServiceItemResponse::fromEntity)
                            .collect(Collectors.toList())
            );
        }

        if (job.getBill() != null) {
            response.setBill(BillResponse.fromEntity(job.getBill()));
        }

        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VehicleResponse getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleResponse vehicle) {
        this.vehicle = vehicle;
    }

    public BayResponse getBay() {
        return bay;
    }

    public void setBay(BayResponse bay) {
        this.bay = bay;
    }

    public MechanicResponse getMechanic() {
        return mechanic;
    }

    public void setMechanic(MechanicResponse mechanic) {
        this.mechanic = mechanic;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getRequestedServices() {
        return requestedServices;
    }

    public void setRequestedServices(String requestedServices) {
        this.requestedServices = requestedServices;
    }

    public String getCustomerComplaints() {
        return customerComplaints;
    }

    public void setCustomerComplaints(String customerComplaints) {
        this.customerComplaints = customerComplaints;
    }

    public String getQualityCheckNotes() {
        return qualityCheckNotes;
    }

    public void setQualityCheckNotes(String qualityCheckNotes) {
        this.qualityCheckNotes = qualityCheckNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public List<ServiceItemResponse> getServiceItems() {
        return serviceItems;
    }

    public void setServiceItems(List<ServiceItemResponse> serviceItems) {
        this.serviceItems = serviceItems;
    }

    public BillResponse getBill() {
        return bill;
    }

    public void setBill(BillResponse bill) {
        this.bill = bill;
    }
}
