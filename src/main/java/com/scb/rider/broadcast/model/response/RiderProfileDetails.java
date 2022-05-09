package com.scb.rider.broadcast.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiderProfileDetails {

  private String id;

  private String riderId;

  private String firstName;

  private String lastName;

  private String availabilityStatus;
  
  private String phoneNumber;

}
