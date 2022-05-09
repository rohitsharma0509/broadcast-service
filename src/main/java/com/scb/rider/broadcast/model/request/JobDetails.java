package com.scb.rider.broadcast.model.request;

import java.util.List;

import javax.validation.constraints.Size;

import com.scb.rider.broadcast.model.kafka.OrderItems;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class JobDetails {

  @Size(max = 40)
  private String jobId;
  private double netPrice;
  @Size(max = 1000)
  private String remark;
  private double totalDistance;
  private List<Location> locationList;
  @Size(max = 40)
  private String orderId;
  private List<OrderItems> orderItems;

}

