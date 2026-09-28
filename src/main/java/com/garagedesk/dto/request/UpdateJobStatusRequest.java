package com.garagedesk.dto.request;

import com.garagedesk.entity.enums.JobStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateJobStatusRequest {

    @NotNull(message = "New job status is required")
    private JobStatus status;

    private String qualityCheckNotes;

    public UpdateJobStatusRequest() {
    }

    public UpdateJobStatusRequest(JobStatus status, String qualityCheckNotes) {
        this.status = status;
        this.qualityCheckNotes = qualityCheckNotes;
    }

    // Getters and Setters
    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getQualityCheckNotes() {
        return qualityCheckNotes;
    }

    public void setQualityCheckNotes(String qualityCheckNotes) {
        this.qualityCheckNotes = qualityCheckNotes;
    }
}
