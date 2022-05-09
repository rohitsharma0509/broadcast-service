package com.scb.rider.broadcast.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class RiderJobDetailsResponse {

  private String id;
  private String profileId;
  private String jobId;
  private String jobStatus;
}
