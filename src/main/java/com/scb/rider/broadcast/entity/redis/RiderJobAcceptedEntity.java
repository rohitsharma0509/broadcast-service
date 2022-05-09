package com.scb.rider.broadcast.entity.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RedisHash("RiderJobAcceptedEntity")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RiderJobAcceptedEntity {
    @Id
    private String jobId;
    
    private String riderId;

    @TimeToLive
    private Long ttl;
}
