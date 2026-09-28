package com.garagedesk.controller;

import com.garagedesk.dto.request.CreateBayRequest;
import com.garagedesk.dto.response.ApiResponse;
import com.garagedesk.dto.response.BayResponse;
import com.garagedesk.service.BayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bays")
@Tag(name = "Bay Management", description = "APIs for service bay scheduling and status monitoring")
public class BayController {

    private final BayService bayService;

    public BayController(BayService bayService) {
        this.bayService = bayService;
    }

    @PostMapping
    @Operation(summary = "Create a service bay")
    public ResponseEntity<ApiResponse<BayResponse>> createBay(@Valid @RequestBody CreateBayRequest request) {
        BayResponse response = bayService.createBay(request);
        return new ResponseEntity<>(ApiResponse.created("Service bay created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all bays with their occupancy status")
    public ResponseEntity<ApiResponse<List<BayResponse>>> getAllBays() {
        return ResponseEntity.ok(ApiResponse.ok(bayService.getAllBays()));
    }

    @GetMapping("/available")
    @Operation(summary = "Get currently free/available service bays")
    public ResponseEntity<ApiResponse<List<BayResponse>>> getAvailableBays() {
        return ResponseEntity.ok(ApiResponse.ok(bayService.getAvailableBays()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bay details by ID")
    public ResponseEntity<ApiResponse<BayResponse>> getBayById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(bayService.getBayById(id)));
    }
}
