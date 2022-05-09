package com.scb.rider.broadcast.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RiderStatus {

  private String riderId;
  private String jobId;
  private String dateTime;
  private String status;

}
