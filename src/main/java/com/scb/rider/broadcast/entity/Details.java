package com.scb.rider.broadcast.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Details {

  private String name;

  private String address;

  private String phone;

  private LatLongLocation location;

  private String subDistrict;
}
