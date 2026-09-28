package com.garagedesk.service.impl;

import com.garagedesk.dto.request.GenerateBillRequest;
import com.garagedesk.dto.response.BillResponse;
import com.garagedesk.entity.Bill;
import com.garagedesk.entity.JobCard;
import com.garagedesk.entity.ServiceItem;
import com.garagedesk.entity.enums.ItemType;
import com.garagedesk.entity.enums.JobStatus;
import com.garagedesk.entity.enums.PaymentStatus;
import com.garagedesk.exception.BadRequestException;
import com.garagedesk.exception.ResourceNotFoundException;
import com.garagedesk.repository.BillRepository;
import com.garagedesk.repository.JobCardRepository;
import com.garagedesk.service.AuditLogService;
import com.garagedesk.service.BillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final JobCardRepository jobCardRepository;
    private final AuditLogService auditLogService;
    private static final AtomicLong INVOICE_SEQUENCE = new AtomicLong(1000);

    public BillServiceImpl(BillRepository billRepository,
                           JobCardRepository jobCardRepository,
                           AuditLogService auditLogService) {
        this.billRepository = billRepository;
        this.jobCardRepository = jobCardRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public BillResponse generateBillForJobCard(Long jobCardId, GenerateBillRequest request) {
        JobCard jobCard = jobCardRepository.findById(jobCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Job Card not found with ID: " + jobCardId));

        if (jobCard.getStatus() == JobStatus.CANCELLED) {
            throw new BadRequestException("Cannot generate a bill for a CANCELLED job card.");
        }

        // If bill already exists, update it rather than throwing an error or duplicate
        Bill bill = billRepository.findByJobCardId(jobCardId).orElse(null);
        if (bill == null) {
            bill = new Bill();
            String dateCode = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            bill.setBillNumber("INV-" + dateCode + "-" + INVOICE_SEQUENCE.incrementAndGet());
            bill.setJobCard(jobCard);
            jobCard.setBill(bill);
        }

        // Compute parts and labour charges
        BigDecimal partsSum = BigDecimal.ZERO;
        BigDecimal labourSum = BigDecimal.ZERO;

        for (ServiceItem item : jobCard.getServiceItems()) {
            if (item.getItemType() == ItemType.PART) {
                partsSum = partsSum.add(item.getTotalPrice());
            } else {
                labourSum = labourSum.add(item.getTotalPrice());
            }
        }

        bill.setPartsTotal(partsSum);
        bill.setLabourCharges(labourSum);

        if (request != null && request.getTaxRate() != null) {
            bill.setTaxRate(request.getTaxRate());
        } else if (bill.getTaxRate() == null) {
            bill.setTaxRate(BigDecimal.valueOf(18.0)); // Default 18% GST
        }

        if (request != null && request.getDiscountAmount() != null) {
            bill.setDiscountAmount(request.getDiscountAmount());
        }

        bill.calculateTotal();

        if (request != null && Boolean.TRUE.equals(request.getMarkAsPaid())) {
            bill.setPaymentStatus(PaymentStatus.PAID);
            bill.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH");
            bill.setPaidAt(LocalDateTime.now());
        }

        Bill saved = billRepository.save(bill);
        auditLogService.log("Bill", saved.getId(), "GENERATE_BILL", "CASHIER",
                String.format("Generated Bill %s for JobCard #%d. Total: %s (Parts: %s, Labour: %s, Tax: %s)",
                        saved.getBillNumber(), jobCardId, saved.getTotalAmount(),
                        saved.getPartsTotal(), saved.getLabourCharges(), saved.getTaxAmount()));

        return BillResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + id));
        return BillResponse.fromEntity(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getBillByJobCardId(Long jobCardId) {
        Bill bill = billRepository.findByJobCardId(jobCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found for Job Card ID: " + jobCardId));
        return BillResponse.fromEntity(bill);
    }

    @Override
    @Transactional
    public BillResponse recordPayment(Long billId, String paymentMethod) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));

        bill.setPaymentStatus(PaymentStatus.PAID);
        bill.setPaymentMethod(paymentMethod != null ? paymentMethod : "CASH");
        bill.setPaidAt(LocalDateTime.now());

        Bill saved = billRepository.save(bill);
        auditLogService.log("Bill", saved.getId(), "RECORD_PAYMENT", "CASHIER",
                "Recorded payment for Bill " + saved.getBillNumber() + " via " + saved.getPaymentMethod());

        return BillResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponse> getAllBills() {
        return billRepository.findAll().stream()
                .map(BillResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
