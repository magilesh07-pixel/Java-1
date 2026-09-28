package com.garagedesk.controller;

import com.garagedesk.dto.request.GenerateBillRequest;
import com.garagedesk.dto.response.ApiResponse;
import com.garagedesk.dto.response.BillResponse;
import com.garagedesk.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@Tag(name = "Billing & Invoicing", description = "APIs for generating service bills from parts and labour charges")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping("/job-card/{jobCardId}")
    @Operation(summary = "5. Generate Final Service Bill", description = "Generates final bill based on parts used and labour charges with applicable taxes")
    public ResponseEntity<ApiResponse<BillResponse>> generateBill(
            @PathVariable Long jobCardId,
            @Valid @RequestBody(required = false) GenerateBillRequest request) {
        BillResponse response = billService.generateBillForJobCard(jobCardId, request != null ? request : new GenerateBillRequest());
        return new ResponseEntity<>(ApiResponse.created("Service bill generated successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Bill by ID")
    public ResponseEntity<ApiResponse<BillResponse>> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(billService.getBillById(id)));
    }

    @GetMapping("/job-card/{jobCardId}")
    @Operation(summary = "Get Bill by Job Card ID")
    public ResponseEntity<ApiResponse<BillResponse>> getBillByJobCardId(@PathVariable Long jobCardId) {
        return ResponseEntity.ok(ApiResponse.ok(billService.getBillByJobCardId(jobCardId)));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Record Payment for Bill", description = "Marks bill as PAID with payment method (CASH, UPI, CARD)")
    public ResponseEntity<ApiResponse<BillResponse>> payBill(
            @PathVariable Long id,
            @RequestParam(defaultValue = "CASH") String paymentMethod) {
        BillResponse response = billService.recordPayment(id, paymentMethod);
        return ResponseEntity.ok(ApiResponse.ok("Payment recorded successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all bills")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getAllBills() {
        return ResponseEntity.ok(ApiResponse.ok(billService.getAllBills()));
    }
}
