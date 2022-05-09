package com.scb.rider.broadcast.model.kafka;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Notification {

  private String title;
  private String body;
  private String sound;
  private String click_action;
}
