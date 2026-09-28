package com.garagedesk.service;

import com.garagedesk.dto.request.GenerateBillRequest;
import com.garagedesk.dto.response.BillResponse;

import java.util.List;

public interface BillService {
    BillResponse generateBillForJobCard(Long jobCardId, GenerateBillRequest request);
    BillResponse getBillById(Long id);
    BillResponse getBillByJobCardId(Long jobCardId);
    BillResponse recordPayment(Long billId, String paymentMethod);
    List<BillResponse> getAllBills();
}
