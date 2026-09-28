package com.garagedesk.entity;

import com.garagedesk.entity.enums.MechanicStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "mechanics")
public class Mechanic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String specialization;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private MechanicStatus status = MechanicStatus.AVAILABLE;

    @Column(precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    public Mechanic() {
    }

    public Mechanic(String name, String specialization, String phone, BigDecimal hourlyRate, MechanicStatus status) {
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.hourlyRate = hourlyRate != null ? hourlyRate : BigDecimal.valueOf(500.00);
        this.status = status != null ? status : MechanicStatus.AVAILABLE;
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
