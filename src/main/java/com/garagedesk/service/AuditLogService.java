package com.garagedesk.service;

import com.garagedesk.dto.response.AuditLogResponse;
import java.util.List;

public interface AuditLogService {
    void log(String entityName, Long entityId, String action, String performedBy, String details);
    List<AuditLogResponse> getLogsForEntity(String entityName, Long entityId);
    List<AuditLogResponse> getAllLogs();
}
