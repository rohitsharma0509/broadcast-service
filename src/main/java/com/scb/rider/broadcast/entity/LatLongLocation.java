package com.scb.rider.broadcast.entity;

import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class LatLongLocation implements Serializable {

  @Size(max = 20, message = "{api.rider.profile.length.msg}")
  private String latitude;
  @Size(max = 20, message = "{api.rider.profile.length.msg}")
  private String longitude;
}
