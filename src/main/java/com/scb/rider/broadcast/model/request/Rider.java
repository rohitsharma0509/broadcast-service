package com.scb.rider.broadcast.model.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Data
public class Rider {

  @NotBlank
  @NotNull
  @Size(max = 40)
  private String riderId;

  @NotBlank
  @NotNull
  private int rank;

  private double distance;

}
