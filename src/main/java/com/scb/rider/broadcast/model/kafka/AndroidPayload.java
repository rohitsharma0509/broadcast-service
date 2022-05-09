package com.scb.rider.broadcast.model.kafka;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@Data
@ToString
public class AndroidPayload {

  private String priority;
  private RiderBroadcastRequest data;

}
