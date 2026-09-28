package com.garagedesk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateVehicleRequest {

    @NotBlank(message = "Vehicle registration number is required")
    private String registrationNumber;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    private Integer manufacturingYear;

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @NotBlank(message = "Owner phone is required")
    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Owner phone must be a valid phone number format")
    private String ownerPhone;

    private String ownerEmail;

    public CreateVehicleRequest() {
    }

    public CreateVehicleRequest(String registrationNumber, String brand, String model, Integer manufacturingYear, String ownerName, String ownerPhone, String ownerEmail) {
        this.registrationNumber = registrationNumber;
        this.brand = brand;
        this.model = model;
        this.manufacturingYear = manufacturingYear;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.ownerEmail = ownerEmail;
    }

    // Getters and Setters
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getManufacturingYear() {
        return manufacturingYear;
    }

    public void setManufacturingYear(Integer manufacturingYear) {
        this.manufacturingYear = manufacturingYear;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}
