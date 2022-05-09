package com.scb.rider.broadcast.model.kafka;

import com.scb.rider.broadcast.constants.JobType;
import com.scb.rider.broadcast.entity.LatLongLocation;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class RiderBroadcastRequest implements Serializable {

  @Size(max = 40)
  private String jobId;
  @Size(max = 200)
  private String merchantName;
  @Size(max = 200)
  private String merchantAddress;
  @Size(max = 20)
  private String merchantPhone;
  private LatLongLocation merchantLocation;
  private String merchantSubDistrict;
  private JobType jobType;
  @Size(max = 200)
  private String customerName;
  @Size(max = 200)
  private String customerAddress;
  @Size(max = 20)
  private String customerPhone;
  private LatLongLocation customerLocation;
  private String customerSubDistrict;
  @Size(max = 40)
  private String expiry;
  @Size(max = 40)
  private String orderId;
  private List<OrderItems> orderItems;
  @Size(max = 1000)
  private String remark;
  private double price;
  private double distance;
  private int jobTimer;
  
  private String currentDateTime;
  private Double minDistanceForJobCompletion;

  //for notification payload
  private String title;
  private String body;
  private String sound;
  private String click_action;
  private String customerRemark;

}
