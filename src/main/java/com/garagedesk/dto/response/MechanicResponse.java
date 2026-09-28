package com.garagedesk.dto.response;

import com.garagedesk.entity.Mechanic;
import com.garagedesk.entity.enums.MechanicStatus;
import java.math.BigDecimal;

public class MechanicResponse {

    private Long id;
    private String name;
    private String specialization;
    private String phone;
    private MechanicStatus status;
    private BigDecimal hourlyRate;

    public MechanicResponse() {
    }

    public static MechanicResponse fromEntity(Mechanic mechanic) {
        if (mechanic == null) return null;
        MechanicResponse response = new MechanicResponse();
        response.setId(mechanic.getId());
        response.setName(mechanic.getName());
        response.setSpecialization(mechanic.getSpecialization());
        response.setPhone(mechanic.getPhone());
        response.setStatus(mechanic.getStatus());
        response.setHourlyRate(mechanic.getHourlyRate());
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public MechanicStatus getStatus() {
        return status;
    }

    public void setStatus(MechanicStatus status) {
        this.status = status;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
}
