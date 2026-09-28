package com.garagedesk.dto.response;

import com.garagedesk.entity.Bill;
import com.garagedesk.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillResponse {

    private Long id;
    private String billNumber;
    private Long jobCardId;
    private String vehicleRegistrationNumber;
    private String ownerName;
    private BigDecimal partsTotal;
    private BigDecimal labourCharges;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private LocalDateTime billingDate;
    private LocalDateTime paidAt;

    public BillResponse() {
    }

    public static BillResponse fromEntity(Bill bill) {
        if (bill == null) return null;
        BillResponse response = new BillResponse();
        response.setId(bill.getId());
        response.setBillNumber(bill.getBillNumber());
        if (bill.getJobCard() != null) {
            response.setJobCardId(bill.getJobCard().getId());
            if (bill.getJobCard().getVehicle() != null) {
                response.setVehicleRegistrationNumber(bill.getJobCard().getVehicle().getRegistrationNumber());
                response.setOwnerName(bill.getJobCard().getVehicle().getOwnerName());
            }
        }
        response.setPartsTotal(bill.getPartsTotal());
        response.setLabourCharges(bill.getLabourCharges());
        response.setTaxRate(bill.getTaxRate());
        response.setTaxAmount(bill.getTaxAmount());
        response.setDiscountAmount(bill.getDiscountAmount());
        response.setTotalAmount(bill.getTotalAmount());
        response.setPaymentStatus(bill.getPaymentStatus());
        response.setPaymentMethod(bill.getPaymentMethod());
        response.setBillingDate(bill.getBillingDate());
        response.setPaidAt(bill.getPaidAt());
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public Long getJobCardId() {
        return jobCardId;
    }

    public void setJobCardId(Long jobCardId) {
        this.jobCardId = jobCardId;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public void setVehicleRegistrationNumber(String vehicleRegistrationNumber) {
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public BigDecimal getPartsTotal() {
        return partsTotal;
    }

    public void setPartsTotal(BigDecimal partsTotal) {
        this.partsTotal = partsTotal;
    }

    public BigDecimal getLabourCharges() {
        return labourCharges;
    }

    public void setLabourCharges(BigDecimal labourCharges) {
        this.labourCharges = labourCharges;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(LocalDateTime billingDate) {
        this.billingDate = billingDate;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
