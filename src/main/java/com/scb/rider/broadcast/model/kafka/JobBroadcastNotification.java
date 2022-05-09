package com.scb.rider.broadcast.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class JobBroadcastNotification {

  private String type;
  private String platform;
  private String payload;
  private String arn;

}
