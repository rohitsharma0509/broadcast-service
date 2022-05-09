package com.scb.rider.broadcast.model.response;

import com.scb.rider.broadcast.entity.RiderEntity;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class BroadcastJobResponse {

  private String jobId;

  private String broadcastStatus;

  private LocalDateTime lastBroadcastDateTime;

  private LocalDateTime expiryTimeForBroadcasting;

  private List<RiderEntity> riderList;

}
