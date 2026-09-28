package com.garagedesk.dto.request;

import com.garagedesk.entity.enums.MechanicStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class CreateMechanicRequest {

    @NotBlank(message = "Mechanic name is required")
    private String name;

    private String specialization;

    private String phone;

    @Positive(message = "Hourly rate must be positive")
    private BigDecimal hourlyRate;

    private MechanicStatus status = MechanicStatus.AVAILABLE;

    public CreateMechanicRequest() {
    }

    public CreateMechanicRequest(String name, String specialization, String phone, BigDecimal hourlyRate) {
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.hourlyRate = hourlyRate;
        this.status = MechanicStatus.AVAILABLE;
    }

    // Getters and Setters
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

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public MechanicStatus getStatus() {
        return status;
    }

    public void setStatus(MechanicStatus status) {
        this.status = status;
    }
}
