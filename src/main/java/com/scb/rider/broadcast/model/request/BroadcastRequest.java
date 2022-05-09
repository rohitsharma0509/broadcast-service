package com.scb.rider.broadcast.model.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BroadcastRequest {

  private JobDetails jobDetails;
  private List<Rider> riders;
  private Integer maxJobsForRider;
  private Integer maxRidersForJob;

}
