package com.garagedesk.controller;

import com.garagedesk.dto.request.AddServiceItemRequest;
import com.garagedesk.dto.request.AssignJobCardRequest;
import com.garagedesk.dto.request.CreateJobCardRequest;
import com.garagedesk.dto.request.UpdateJobStatusRequest;
import com.garagedesk.dto.response.ApiResponse;
import com.garagedesk.dto.response.JobCardResponse;
import com.garagedesk.entity.enums.JobStatus;
import com.garagedesk.service.JobCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-cards")
@Tag(name = "Job Card & Bay Scheduling", description = "Core APIs for Job Card lifecycle, bay scheduling, and progress tracking")
public class JobCardController {

    private final JobCardService jobCardService;

    public JobCardController(JobCardService jobCardService) {
        this.jobCardService = jobCardService;
    }

    @PostMapping
    @Operation(summary = "1. Create a Job Card", description = "Creates a new job card with vehicle details and requested services (Status: WAITING)")
    public ResponseEntity<ApiResponse<JobCardResponse>> createJobCard(@Valid @RequestBody CreateJobCardRequest request) {
        JobCardResponse response = jobCardService.createJobCard(request);
        return new ResponseEntity<>(ApiResponse.created("Job card created successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/assign")
    @Operation(
            summary = "2 & 4. Assign Job Card to Bay and Mechanic",
            description = "Assigns an available bay and mechanic. Enforces Business Rule 1: Rejects if the bay is already occupied by an unfinished job."
    )
    public ResponseEntity<ApiResponse<JobCardResponse>> assignJobCard(
            @PathVariable Long id,
            @Valid @RequestBody AssignJobCardRequest request) {
        JobCardResponse response = jobCardService.assignJobCard(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Job card assigned to bay and mechanic successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "3. Update Job Card Status",
            description = "Transitions job status (WAITING -> IN_PROGRESS -> QUALITY_CHECK -> COMPLETED). Enforces Business Rule 2: Cannot complete without passing QUALITY_CHECK."
    )
    public ResponseEntity<ApiResponse<JobCardResponse>> updateJobStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobStatusRequest request) {
        JobCardResponse response = jobCardService.updateJobStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Job status updated successfully to " + request.getStatus(), response));
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Add Part or Labour Service to Job Card", description = "Records parts used or labour hours on the active job card")
    public ResponseEntity<ApiResponse<JobCardResponse>> addServiceItem(
            @PathVariable Long id,
            @Valid @RequestBody AddServiceItemRequest request) {
        JobCardResponse response = jobCardService.addServiceItem(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Service item added successfully", response));
    }

    @DeleteMapping("/{jobCardId}/items/{itemId}")
    @Operation(summary = "Remove Part or Service Item from Job Card")
    public ResponseEntity<ApiResponse<JobCardResponse>> removeServiceItem(
            @PathVariable Long jobCardId,
            @PathVariable Long itemId) {
        JobCardResponse response = jobCardService.removeServiceItem(jobCardId, itemId);
        return ResponseEntity.ok(ApiResponse.ok("Service item removed successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full Job Card details by ID")
    public ResponseEntity<ApiResponse<JobCardResponse>> getJobCardById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(jobCardService.getJobCardById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all Job Cards", description = "Optionally filter by status (WAITING, IN_PROGRESS, QUALITY_CHECK, COMPLETED)")
    public ResponseEntity<ApiResponse<List<JobCardResponse>>> getAllJobCards(
            @Parameter(description = "Optional filter by status") @RequestParam(required = false) JobStatus status) {
        List<JobCardResponse> list = (status != null)
                ? jobCardService.getJobCardsByStatus(status)
                : jobCardService.getAllJobCards();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get all Job Cards for a vehicle")
    public ResponseEntity<ApiResponse<List<JobCardResponse>>> getJobCardsByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(ApiResponse.ok(jobCardService.getJobCardsByVehicle(vehicleId)));
    }
}
