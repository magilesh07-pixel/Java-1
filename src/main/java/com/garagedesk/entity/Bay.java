package com.garagedesk.entity;

import com.garagedesk.entity.enums.BayStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "bays", indexes = {
    @Index(name = "idx_bay_number", columnList = "bayNumber", unique = true)
})
public class Bay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String bayNumber;

    @Column(length = 100)
    private String bayType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private BayStatus status = BayStatus.AVAILABLE;

    public Bay() {
    }

    public Bay(String bayNumber, String bayType, BayStatus status) {
        this.bayNumber = bayNumber;
        this.bayType = bayType;
        this.status = status != null ? status : BayStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return this.status == BayStatus.AVAILABLE;
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
}
