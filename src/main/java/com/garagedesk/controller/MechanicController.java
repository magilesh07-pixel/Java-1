package com.garagedesk.controller;

import com.garagedesk.dto.request.CreateMechanicRequest;
import com.garagedesk.dto.response.ApiResponse;
import com.garagedesk.dto.response.MechanicResponse;
import com.garagedesk.service.MechanicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mechanics")
@Tag(name = "Mechanic Management", description = "APIs for registering and tracking garage technicians")
public class MechanicController {

    private final MechanicService mechanicService;

    public MechanicController(MechanicService mechanicService) {
        this.mechanicService = mechanicService;
    }

    @PostMapping
    @Operation(summary = "Add a new mechanic")
    public ResponseEntity<ApiResponse<MechanicResponse>> createMechanic(@Valid @RequestBody CreateMechanicRequest request) {
        MechanicResponse response = mechanicService.createMechanic(request);
        return new ResponseEntity<>(ApiResponse.created("Mechanic registered successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all mechanics")
    public ResponseEntity<ApiResponse<List<MechanicResponse>>> getAllMechanics() {
        return ResponseEntity.ok(ApiResponse.ok(mechanicService.getAllMechanics()));
    }

    @GetMapping("/available")
    @Operation(summary = "Get currently available mechanics")
    public ResponseEntity<ApiResponse<List<MechanicResponse>>> getAvailableMechanics() {
        return ResponseEntity.ok(ApiResponse.ok(mechanicService.getAvailableMechanics()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get mechanic by ID")
    public ResponseEntity<ApiResponse<MechanicResponse>> getMechanicById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(mechanicService.getMechanicById(id)));
    }
}
