package com.garagedesk.repository;

import com.garagedesk.entity.Bay;
import com.garagedesk.entity.enums.BayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BayRepository extends JpaRepository<Bay, Long> {
    Optional<Bay> findByBayNumber(String bayNumber);
    boolean existsByBayNumber(String bayNumber);
    List<Bay> findByStatus(BayStatus status);
}
