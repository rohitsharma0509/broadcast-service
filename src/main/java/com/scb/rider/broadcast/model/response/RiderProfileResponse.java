package com.scb.rider.broadcast.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiderProfileResponse {

  @JsonProperty("riderProfileDto")
  private RiderProfileDetails riderProfileDetails;

  @JsonProperty("riderDeviceDetails")
  private RiderDeviceDetailsResponse riderDeviceDetails;


}
