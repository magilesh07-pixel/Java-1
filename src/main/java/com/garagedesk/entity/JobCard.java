package com.garagedesk.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.garagedesk.entity.enums.JobStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_cards")
public class JobCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bay_id")
    private Bay bay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mechanic_id")
    private Mechanic mechanic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobStatus status = JobStatus.WAITING;

    @Column(length = 500)
    private String requestedServices;

    @Column(length = 500)
    private String customerComplaints;

    @Column(length = 500)
    private String qualityCheckNotes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "jobCard", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ServiceItem> serviceItems = new ArrayList<>();

    @OneToOne(mappedBy = "jobCard", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Bill bill;

    public JobCard() {
    }

    public JobCard(Vehicle vehicle, String requestedServices, String customerComplaints) {
        this.vehicle = vehicle;
        this.requestedServices = requestedServices;
        this.customerComplaints = customerComplaints;
        this.status = JobStatus.WAITING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = JobStatus.WAITING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addServiceItem(ServiceItem item) {
        serviceItems.add(item);
        item.setJobCard(this);
    }

    public void removeServiceItem(ServiceItem item) {
        serviceItems.remove(item);
        item.setJobCard(null);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Bay getBay() {
        return bay;
    }

    public void setBay(Bay bay) {
        this.bay = bay;
    }

    public Mechanic getMechanic() {
        return mechanic;
    }

    public void setMechanic(Mechanic mechanic) {
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

    public List<ServiceItem> getServiceItems() {
        return serviceItems;
    }

    public void setServiceItems(List<ServiceItem> serviceItems) {
        this.serviceItems = serviceItems;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }
}
