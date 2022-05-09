package com.scb.rider.broadcast.model.kafka;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Alert {

  private String title;
  private String body;

}
