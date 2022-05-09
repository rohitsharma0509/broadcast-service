package com.scb.rider.broadcast.entity.redis;

import com.scb.rider.broadcast.model.kafka.RiderBroadcastRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Map;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("RiderJobEntity")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RiderJobEntity {
    @Id
    private String riderId;
    private Map<String, RiderBroadcastRequest> jobMap;

    @TimeToLive
    private Long ttl;
}
