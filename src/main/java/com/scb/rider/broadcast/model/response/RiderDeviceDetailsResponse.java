package com.scb.rider.broadcast.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiderDeviceDetailsResponse {

  private String id;

  private String platform;

  private String arn;

}
