package com.scb.rider.broadcast.model.kafka;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Aps {

  private Alert alert;
  private int badge;
  private String sound;

}
