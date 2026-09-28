package com.garagedesk.dto.response;

import com.garagedesk.entity.Bay;
import com.garagedesk.entity.enums.BayStatus;

public class BayResponse {

    private Long id;
    private String bayNumber;
    private String bayType;
    private BayStatus status;
    private boolean available;
    private Long activeJobCardId;

    public BayResponse() {
    }

    public static BayResponse fromEntity(Bay bay) {
        if (bay == null) return null;
        BayResponse response = new BayResponse();
        response.setId(bay.getId());
        response.setBayNumber(bay.getBayNumber());
        response.setBayType(bay.getBayType());
        response.setStatus(bay.getStatus());
        response.setAvailable(bay.isAvailable());
        return response;
    }

    public static BayResponse fromEntity(Bay bay, Long activeJobCardId) {
        BayResponse response = fromEntity(bay);
        if (response != null) {
            response.setActiveJobCardId(activeJobCardId);
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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Long getActiveJobCardId() {
        return activeJobCardId;
    }

    public void setActiveJobCardId(Long activeJobCardId) {
        this.activeJobCardId = activeJobCardId;
    }
}
