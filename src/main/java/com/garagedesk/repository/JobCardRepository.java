package com.garagedesk.repository;

import com.garagedesk.entity.JobCard;
import com.garagedesk.entity.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobCardRepository extends JpaRepository<JobCard, Long> {

    List<JobCard> findByStatus(JobStatus status);

    List<JobCard> findByVehicleId(Long vehicleId);

    List<JobCard> findByBayId(Long bayId);

    List<JobCard> findByMechanicId(Long mechanicId);

    /**
     * Checks if a bay is currently occupied by an active job card.
     * Active statuses are WAITING, IN_PROGRESS, and QUALITY_CHECK.
     */
    boolean existsByBayIdAndStatusIn(Long bayId, Collection<JobStatus> statuses);

    Optional<JobCard> findFirstByBayIdAndStatusIn(Long bayId, Collection<JobStatus> statuses);

    @Query("SELECT j FROM JobCard j WHERE j.bay.id = :bayId AND j.status NOT IN ('COMPLETED', 'CANCELLED')")
    Optional<JobCard> findActiveJobByBayId(@Param("bayId") Long bayId);
}
