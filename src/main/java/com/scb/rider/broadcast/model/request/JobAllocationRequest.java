package com.scb.rider.broadcast.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class JobAllocationRequest {

  private String jobDetail;
  private String jobId;
  private String jobDate;
  private String jobStatus;
  private String jobStatusEn;
  private String jobStatusTh;
  private String jobDesc;
  private String startTime;
  private String finishTime;
  private String haveReturn;
  private String jobType;
  private String option;
  private Float totalDistance;
  private Float totalWeight;
  private Float totalSize;
  private String remark;
  private Integer userType;
  private Float normalPrice;
  private Float netPrice;
  private Float discount;
  private Integer rating;
  private Location[] locationList;
}


