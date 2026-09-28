package com.garagedesk.repository;

import com.garagedesk.entity.Mechanic;
import com.garagedesk.entity.enums.MechanicStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MechanicRepository extends JpaRepository<Mechanic, Long> {
    List<Mechanic> findByStatus(MechanicStatus status);
}
