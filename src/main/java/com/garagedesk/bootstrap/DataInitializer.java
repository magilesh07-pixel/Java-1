package com.garagedesk.bootstrap;

import com.garagedesk.entity.*;
import com.garagedesk.entity.enums.*;
import com.garagedesk.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final VehicleRepository vehicleRepository;
    private final BayRepository bayRepository;
    private final MechanicRepository mechanicRepository;
    private final JobCardRepository jobCardRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final UserRepository userRepository;

    public DataInitializer(VehicleRepository vehicleRepository,
                           BayRepository bayRepository,
                           MechanicRepository mechanicRepository,
                           JobCardRepository jobCardRepository,
                           ServiceItemRepository serviceItemRepository,
                           UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.bayRepository = bayRepository;
        this.mechanicRepository = mechanicRepository;
        this.jobCardRepository = jobCardRepository;
        this.serviceItemRepository = serviceItemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        // Ensure real authenticated users are always seeded
        if (userRepository.count() == 0) {
            userRepository.save(new User("admin", "admin@garagedesk.com", "garage2026", "Mahilesh", UserRole.WORKSHOP_MANAGER, "SECE-060", "LOCAL"));
            userRepository.save(new User("client", "client@garagedesk.com", "client123", "Priya Sharma", UserRole.CLIENT, "CLIENT-01", "LOCAL"));
            userRepository.save(new User("advisor", "advisor@garagedesk.com", "advisor123", "Arun Kumar", UserRole.SERVICE_ADVISOR, "ADV-01", "LOCAL"));
            userRepository.save(new User("mechanic", "mechanic@garagedesk.com", "mech123", "Rajesh Kumar", UserRole.MECHANIC, "MCH-01", "LOCAL"));
            userRepository.save(new User("inspector", "inspector@garagedesk.com", "qc123", "Suresh Babu", UserRole.QUALITY_INSPECTOR, "QC-01", "LOCAL"));
            log.info("Seeded 5 Authenticated User Accounts (admin, client, advisor, mechanic, inspector).");
        }

        if (bayRepository.count() > 0) {
            log.info("Database already seeded. Skipping bay and vehicle initialization.");
            return;
        }

        log.info("--- Initializing GarageDesk Sample Data ---");

        // 1. Seed Service Bays
        Bay bay1 = bayRepository.save(new Bay("Bay 1", "Express Lube & Quick Service", BayStatus.OCCUPIED));
        Bay bay2 = bayRepository.save(new Bay("Bay 2", "General Mechanical & Suspension", BayStatus.AVAILABLE));
        Bay bay3 = bayRepository.save(new Bay("Bay 3", "Wheel Alignment & Balancing", BayStatus.AVAILABLE));
        Bay bay4 = bayRepository.save(new Bay("Bay 4", "Computer Diagnostics & Electrical", BayStatus.AVAILABLE));
        log.info("Seeded 4 Service Bays.");

        // 2. Seed Mechanics
        Mechanic mech1 = mechanicRepository.save(new Mechanic("Rajesh Kumar", "Senior Engine Specialist", "9876543210", BigDecimal.valueOf(650.00), MechanicStatus.BUSY));
        Mechanic mech2 = mechanicRepository.save(new Mechanic("Suresh Babu", "Brake & Suspension", "9876543211", BigDecimal.valueOf(500.00), MechanicStatus.AVAILABLE));
        Mechanic mech3 = mechanicRepository.save(new Mechanic("Anand Prakash", "Diagnostics & Electrical", "9876543212", BigDecimal.valueOf(550.00), MechanicStatus.AVAILABLE));
        log.info("Seeded 3 Mechanics.");

        // 3. Seed Vehicles
        Vehicle veh1 = vehicleRepository.save(new Vehicle("TN-38-BZ-4521", "Toyota", "Innova Crysta", 2021, "Arun Kumar", "9842100001", "arun@example.com"));
        Vehicle veh2 = vehicleRepository.save(new Vehicle("TN-37-CK-9912", "Honda", "City ZX", 2022, "Priya Sharma", "9842100002", "priya@example.com"));
        Vehicle veh3 = vehicleRepository.save(new Vehicle("TN-66-E-1004", "Hyundai", "Creta SX", 2020, "Karthik Raja", "9842100003", "karthik@example.com"));
        log.info("Seeded 3 Customer Vehicles.");

        // 4. Seed Active JobCard in Bay 1 (to test Bay Conflict Rule immediately!)
        JobCard job1 = new JobCard(veh1, "Engine oil flush, synthetic 5W-30 replacement, brake pad check", "Squeaking sound when braking");
        job1.setBay(bay1);
        job1.setMechanic(mech1);
        job1.setStatus(JobStatus.IN_PROGRESS);
        JobCard savedJob1 = jobCardRepository.save(job1);

        ServiceItem item1 = new ServiceItem(savedJob1, "Castrol Edge 5W-30 Fully Synthetic Oil (4L)", ItemType.PART, 1, BigDecimal.valueOf(2850.00));
        ServiceItem item2 = new ServiceItem(savedJob1, "Genuine OEM Oil Filter", ItemType.PART, 1, BigDecimal.valueOf(450.00));
        ServiceItem item3 = new ServiceItem(savedJob1, "General Service Labour & Inspection", ItemType.LABOUR_SERVICE, 2, BigDecimal.valueOf(650.00));
        serviceItemRepository.save(item1);
        serviceItemRepository.save(item2);
        serviceItemRepository.save(item3);
        log.info("Seeded active JobCard #{} in Bay 1 with parts and labour.", savedJob1.getId());

        // 5. Seed Waiting JobCard for veh2
        JobCard job2 = new JobCard(veh2, "Periodic 20,000 km Service & AC inspection", "AC cooling is slightly low");
        job2.setStatus(JobStatus.WAITING);
        jobCardRepository.save(job2);

        log.info("--- GarageDesk Data Seeding Completed Successfully ---");
    }
}
