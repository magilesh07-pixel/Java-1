package com.garagedesk.service;

import com.garagedesk.dto.request.AddServiceItemRequest;
import com.garagedesk.dto.request.AssignJobCardRequest;
import com.garagedesk.dto.request.CreateJobCardRequest;
import com.garagedesk.dto.request.UpdateJobStatusRequest;
import com.garagedesk.dto.response.JobCardResponse;
import com.garagedesk.entity.JobCard;
import com.garagedesk.entity.enums.JobStatus;

import java.util.List;

public interface JobCardService {
    JobCardResponse createJobCard(CreateJobCardRequest request);
    JobCardResponse assignJobCard(Long id, AssignJobCardRequest request);
    JobCardResponse updateJobStatus(Long id, UpdateJobStatusRequest request);
    JobCardResponse addServiceItem(Long id, AddServiceItemRequest request);
    JobCardResponse removeServiceItem(Long jobCardId, Long serviceItemId);
    JobCardResponse getJobCardById(Long id);
    List<JobCardResponse> getAllJobCards();
    List<JobCardResponse> getJobCardsByStatus(JobStatus status);
    List<JobCardResponse> getJobCardsByVehicle(Long vehicleId);
    JobCard getJobCardEntity(Long id);
}
