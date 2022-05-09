package com.scb.rider.broadcast.model.response;

import com.scb.rider.broadcast.constants.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobEntity {
    private JobType jobTypeEnum;
    private List<JobLocation> locationList;
    private Double minDistanceForJobCompletion;
    private String creationDateTime;
}
