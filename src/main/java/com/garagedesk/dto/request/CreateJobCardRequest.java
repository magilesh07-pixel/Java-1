package com.garagedesk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateJobCardRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotBlank(message = "Requested services description is required")
    private String requestedServices;

    private String customerComplaints;

    private List<AddServiceItemRequest> initialItems;

    public CreateJobCardRequest() {
    }

    public CreateJobCardRequest(Long vehicleId, String requestedServices, String customerComplaints) {
        this.vehicleId = vehicleId;
        this.requestedServices = requestedServices;
        this.customerComplaints = customerComplaints;
    }

    // Getters and Setters
    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
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

    public List<AddServiceItemRequest> getInitialItems() {
        return initialItems;
    }

    public void setInitialItems(List<AddServiceItemRequest> initialItems) {
        this.initialItems = initialItems;
    }
}
