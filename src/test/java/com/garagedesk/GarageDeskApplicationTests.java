package com.garagedesk;

import com.garagedesk.dto.request.*;
import com.garagedesk.dto.response.BillResponse;
import com.garagedesk.dto.response.JobCardResponse;
import com.garagedesk.dto.response.VehicleResponse;
import com.garagedesk.entity.enums.ItemType;
import com.garagedesk.entity.enums.JobStatus;
import com.garagedesk.exception.BayConflictException;
import com.garagedesk.exception.InvalidStatusTransitionException;
import com.garagedesk.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GarageDeskApplicationTests {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private BayService bayService;

    @Autowired
    private MechanicService mechanicService;

    @Autowired
    private JobCardService jobCardService;

    @Autowired
    private BillService billService;

    @Test
    @DisplayName("Test 1: Context loads successfully")
    void contextLoads() {
        assertNotNull(vehicleService);
        assertNotNull(jobCardService);
        assertNotNull(bayService);
        assertNotNull(mechanicService);
        assertNotNull(billService);
    }

    @Test
    @DisplayName("Test 2 (Business Rule 1): Prevent assigning a bay that is already occupied")
    void testBayConflictPrevention() {
        // Vehicle 1 already occupies Bay 1 from DataInitializer
        // Create a new job card for Vehicle 2
        VehicleResponse veh2 = vehicleService.getVehicleByRegistration("TN-37-CK-9912");
        CreateJobCardRequest jobReq = new CreateJobCardRequest(veh2.getId(), "Oil change", "Routine check");
        JobCardResponse newJob = jobCardService.createJobCard(jobReq);

        // Attempt to assign Bay 1 (ID 1), which is already occupied by Job #1
        AssignJobCardRequest assignReq = new AssignJobCardRequest(1L, 1L);

        BayConflictException exception = assertThrows(BayConflictException.class, () -> {
            jobCardService.assignJobCard(newJob.getId(), assignReq);
        });

        assertTrue(exception.getMessage().contains("Bay Conflict"));
        assertTrue(exception.getMessage().contains("already occupied"));
    }

    @Test
    @DisplayName("Test 3 (Business Rule 2): Reject moving to COMPLETED before passing QUALITY_CHECK")
    void testQualityCheckEnforcementBeforeCompleted() {
        // Create vehicle & job card
        CreateVehicleRequest vehReq = new CreateVehicleRequest(
                "KA-01-MJ-5566", "Maruti", "Swift", 2019, "Ramesh Rao", "9876501234", "ramesh@test.com"
        );
        VehicleResponse vehicle = vehicleService.registerVehicle(vehReq);

        CreateJobCardRequest jobReq = new CreateJobCardRequest(vehicle.getId(), "Brake overhaul", "Brakes grinding");
        JobCardResponse job = jobCardService.createJobCard(jobReq);

        // Assign to Bay 2 (available)
        jobCardService.assignJobCard(job.getId(), new AssignJobCardRequest(2L, 2L));

        // Job is now IN_PROGRESS. Attempting to directly complete without QUALITY_CHECK must fail!
        UpdateJobStatusRequest invalidComplete = new UpdateJobStatusRequest(JobStatus.COMPLETED, null);

        InvalidStatusTransitionException exception = assertThrows(InvalidStatusTransitionException.class, () -> {
            jobCardService.updateJobStatus(job.getId(), invalidComplete);
        });

        assertTrue(exception.getMessage().contains("QUALITY_CHECK"));
    }

    @Test
    @DisplayName("Test 4: Full Valid Lifecycle (WAITING -> IN_PROGRESS -> QUALITY_CHECK -> COMPLETED) and Billing")
    void testFullWorkflowAndBilling() {
        // 1. Create vehicle
        CreateVehicleRequest vehReq = new CreateVehicleRequest(
                "KL-07-ZZ-7788", "Hyundai", "i20", 2023, "Devika Nair", "9876549900", "devika@test.com"
        );
        VehicleResponse vehicle = vehicleService.registerVehicle(vehReq);

        // 2. Create job card (Feature 1: Status WAITING)
        CreateJobCardRequest jobReq = new CreateJobCardRequest(vehicle.getId(), "Full synthetic service", "Mileage drop");
        JobCardResponse job = jobCardService.createJobCard(jobReq);
        assertEquals(JobStatus.WAITING, job.getStatus());

        // 3. Assign to Bay 3 (Feature 2: Bay scheduling)
        job = jobCardService.assignJobCard(job.getId(), new AssignJobCardRequest(3L, 3L));
        assertEquals(JobStatus.IN_PROGRESS, job.getStatus());

        // 4. Add parts and labor
        job = jobCardService.addServiceItem(job.getId(), new AddServiceItemRequest(
                "Synthetic Engine Oil 4L", ItemType.PART, 1, BigDecimal.valueOf(2500.00)
        ));
        job = jobCardService.addServiceItem(job.getId(), new AddServiceItemRequest(
                "Oil Filter Replacement", ItemType.PART, 1, BigDecimal.valueOf(400.00)
        ));
        job = jobCardService.addServiceItem(job.getId(), new AddServiceItemRequest(
                "Labor Charge - 2 Hours", ItemType.LABOUR_SERVICE, 2, BigDecimal.valueOf(500.00)
        ));
        assertEquals(3, job.getServiceItems().size());

        // 5. Move to QUALITY_CHECK (Feature 3)
        job = jobCardService.updateJobStatus(job.getId(), new UpdateJobStatusRequest(
                JobStatus.QUALITY_CHECK, "Passed 24-point safety and road test"
        ));
        assertEquals(JobStatus.QUALITY_CHECK, job.getStatus());

        // 6. Move to COMPLETED (Allowed now that QUALITY_CHECK passed)
        job = jobCardService.updateJobStatus(job.getId(), new UpdateJobStatusRequest(JobStatus.COMPLETED, null));
        assertEquals(JobStatus.COMPLETED, job.getStatus());
        assertNotNull(job.getCompletedAt());

        // 7. Generate Bill (Feature 5: Parts + Labour + Tax)
        // Parts: 2500 + 400 = 2900
        // Labour: 2 * 500 = 1000
        // Subtotal: 3900
        // Tax (18% of 3900): 702.00
        // Total: 4602.00
        GenerateBillRequest billReq = new GenerateBillRequest(BigDecimal.valueOf(18.0), BigDecimal.ZERO, "UPI", true);
        BillResponse bill = billService.generateBillForJobCard(job.getId(), billReq);

        assertNotNull(bill);
        assertEquals(0, BigDecimal.valueOf(2900.00).compareTo(bill.getPartsTotal()));
        assertEquals(0, BigDecimal.valueOf(1000.00).compareTo(bill.getLabourCharges()));
        assertEquals(0, BigDecimal.valueOf(702.00).compareTo(bill.getTaxAmount()));
        assertEquals(0, BigDecimal.valueOf(4602.00).compareTo(bill.getTotalAmount()));
    }
}
