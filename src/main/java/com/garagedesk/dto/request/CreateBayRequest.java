package com.garagedesk.dto.request;

import com.garagedesk.entity.enums.BayStatus;
import jakarta.validation.constraints.NotBlank;

public class CreateBayRequest {

    @NotBlank(message = "Bay number/name is required (e.g., 'Bay 1')")
    private String bayNumber;

    private String bayType;

    private BayStatus status = BayStatus.AVAILABLE;

    public CreateBayRequest() {
    }

    public CreateBayRequest(String bayNumber, String bayType) {
        this.bayNumber = bayNumber;
        this.bayType = bayType;
        this.status = BayStatus.AVAILABLE;
    }

    // Getters and Setters
    public String getBayNumber() {
        return bayNumber;
    }

    public void setBayNumber(String bayNumber) {
        this.bayNumber = bayNumber;
    }

    public String getBayType() {
        return bayType;
    }

    public void setBayType(String bayType) {
        this.bayType = bayType;
    }

    public BayStatus getStatus() {
        return status;
    }

    public void setStatus(BayStatus status) {
        this.status = status;
    }
}
