package com.scb.rider.broadcast.model.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Location {

  @Size(max = 200)
  private String addressName;
  @Size(max = 200)
  private String address;
  @NotBlank
  @Size(max = 20)
  private String lat;
  @NotBlank
  @Size(max = 20)
  private String lng;
  @Size(max = 200)
  private String contactName;
  @Size(max = 20)
  private String contactPhone;
  @Size(max = 20)
  private String cashFee;
  @NotNull
  private int seq;

}
