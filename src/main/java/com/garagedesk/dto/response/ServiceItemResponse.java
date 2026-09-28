package com.garagedesk.dto.response;

import com.garagedesk.entity.ServiceItem;
import com.garagedesk.entity.enums.ItemType;
import java.math.BigDecimal;

public class ServiceItemResponse {

    private Long id;
    private String itemName;
    private ItemType itemType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public ServiceItemResponse() {
    }

    public static ServiceItemResponse fromEntity(ServiceItem item) {
        if (item == null) return null;
        ServiceItemResponse response = new ServiceItemResponse();
        response.setId(item.getId());
        response.setItemName(item.getItemName());
        response.setItemType(item.getItemType());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setTotalPrice(item.getTotalPrice());
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
