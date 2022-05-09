package com.scb.rider.broadcast.model.kafka;

import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItems implements Serializable {
  
  @Size(max = 100)
  private String name;
  private int quantity;

}
