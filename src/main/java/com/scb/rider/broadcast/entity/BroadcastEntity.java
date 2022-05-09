package com.scb.rider.broadcast.entity;

import com.scb.rider.broadcast.constants.JobType;
import com.scb.rider.broadcast.model.kafka.OrderItems;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "broadcast")
@Getter
@Setter
@Builder
@CompoundIndex(name="status_broadcast_time", def = "{'broadcastStatus': 1, 'expiryTimeForBroadcasting': 1}")
public class BroadcastEntity {

  @Id private String id;

  @Indexed(name = "jobId", unique = true)
  private String jobId;

  private JobType jobType;

  private Details merchantDetails;

  private Details customerDetails;

  private LocalDateTime expiryTimeForBroadcasting;

  private double price;

  private double distance;

  private String remark;

  private String broadcastStatus;

  @Indexed(name="lastBroadcastDateTime")
  private LocalDateTime lastBroadcastDateTime;

  private Integer maxJobsForRider;

  private Integer maxRidersForJob;

  private List<RiderEntity> riderEntities;

  private int batchCount;

  private String orderId;

  private List<OrderItems> orderItems;
  
  private Double minDistanceForJobCompletion;

  @CreatedDate
  @Indexed(name="createdDateTime")
  private LocalDateTime createdDateTime;

  private String customerRemark;
}
