package com.garagedesk.controller;

import com.garagedesk.dto.request.CreateVehicleRequest;
import com.garagedesk.dto.response.ApiResponse;
import com.garagedesk.dto.response.VehicleResponse;
import com.garagedesk.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicle Management", description = "APIs for registering and retrieving customer vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @Operation(summary = "Register a new vehicle", description = "Adds a new customer vehicle with owner details")
    public ResponseEntity<ApiResponse<VehicleResponse>> registerVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        VehicleResponse response = vehicleService.registerVehicle(request);
        return new ResponseEntity<>(ApiResponse.created("Vehicle registered successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable Long id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/registration/{regNum}")
    @Operation(summary = "Get vehicle by registration number")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleByRegistration(@PathVariable String regNum) {
        VehicleResponse response = vehicleService.getVehicleByRegistration(regNum);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(summary = "Get all registered vehicles")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllVehicles() {
        List<VehicleResponse> list = vehicleService.getAllVehicles();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}
