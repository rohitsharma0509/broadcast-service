package com.scb.rider.broadcast.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiderEntity {

  @Indexed(name="riderId")
  private String riderId;

  private String broadcastStatus;

  private double distance;

  private LocalDateTime expiryTimeForBroadcasting;

  private int  batchCount;

  private int jobTimer;
}
