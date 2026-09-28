package com.garagedesk.dto.response;

import com.garagedesk.entity.AuditLog;
import java.time.LocalDateTime;

public class AuditLogResponse {

    private Long id;
    private String entityName;
    private Long entityId;
    private String action;
    private String performedBy;
    private String details;
    private LocalDateTime timestamp;

    public AuditLogResponse() {
    }

    public static AuditLogResponse fromEntity(AuditLog log) {
        if (log == null) return null;
        AuditLogResponse res = new AuditLogResponse();
        res.setId(log.getId());
        res.setEntityName(log.getEntityName());
        res.setEntityId(log.getEntityId());
        res.setAction(log.getAction());
        res.setPerformedBy(log.getPerformedBy());
        res.setDetails(log.getDetails());
        res.setTimestamp(log.getTimestamp());
        return res;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
