package com.garagedesk.dto.request;

import jakarta.validation.constraints.NotNull;

public class AssignJobCardRequest {

    @NotNull(message = "Bay ID is required")
    private Long bayId;

    private Long mechanicId;

    public AssignJobCardRequest() {
    }

    public AssignJobCardRequest(Long bayId, Long mechanicId) {
        this.bayId = bayId;
        this.mechanicId = mechanicId;
    }

    // Getters and Setters
    public Long getBayId() {
        return bayId;
    }

    public void setBayId(Long bayId) {
        this.bayId = bayId;
    }

    public Long getMechanicId() {
        return mechanicId;
    }

    public void setMechanicId(Long mechanicId) {
        this.mechanicId = mechanicId;
    }
}
