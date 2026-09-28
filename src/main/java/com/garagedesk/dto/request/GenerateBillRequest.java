package com.garagedesk.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class GenerateBillRequest {

    @PositiveOrZero(message = "Tax rate cannot be negative")
    private BigDecimal taxRate;

    @PositiveOrZero(message = "Discount amount cannot be negative")
    private BigDecimal discountAmount;

    private String paymentMethod;

    private Boolean markAsPaid;

    public GenerateBillRequest() {
    }

    public GenerateBillRequest(BigDecimal taxRate, BigDecimal discountAmount, String paymentMethod, Boolean markAsPaid) {
        this.taxRate = taxRate;
        this.discountAmount = discountAmount;
        this.paymentMethod = paymentMethod;
        this.markAsPaid = markAsPaid;
    }

    // Getters and Setters
    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Boolean getMarkAsPaid() {
        return markAsPaid;
    }

    public void setMarkAsPaid(Boolean markAsPaid) {
        this.markAsPaid = markAsPaid;
    }
}
