package com.garagedesk.controller;

import com.garagedesk.dto.response.ApiResponse;
import com.garagedesk.dto.response.AuditLogResponse;
import com.garagedesk.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Log & Accountability", description = "APIs for tracking changes, status transitions, and operations")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Operation(summary = "Get all audit trail logs")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAllLogs() {
        return ResponseEntity.ok(ApiResponse.ok(auditLogService.getAllLogs()));
    }

    @GetMapping("/{entityName}/{entityId}")
    @Operation(summary = "Get audit logs for a specific entity (e.g. JobCard, 1)")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getEntityLogs(
            @PathVariable String entityName,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(ApiResponse.ok(auditLogService.getLogsForEntity(entityName, entityId)));
    }
}
