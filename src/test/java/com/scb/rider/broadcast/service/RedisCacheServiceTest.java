package com.scb.rider.broadcast.service;

import com.scb.rider.broadcast.constants.BroadcastServiceConstants;
import com.scb.rider.broadcast.model.kafka.RiderBroadcastRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class RedisCacheServiceTest {

  private static final String RIDER_ID = "RR00001";
  private static final String JOB_ID = "S1100000001";
  private static final int ONE = 1;
  private static final long EXPIRY_TIME = 10;

  @InjectMocks
  private RedisCacheService redisCacheService;

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  @Mock
  private HashOperations<String, Object, Object> hashOperations;

  @BeforeEach
  public void setup() {
    ReflectionTestUtils.setField(redisCacheService, "hashOperations", hashOperations);
  }

  @Test
  void testPutForDefaultExpiry() {
    redisCacheService.put(RIDER_ID, JOB_ID, RiderBroadcastRequest.builder().jobId(JOB_ID).build());
    verify(redisTemplate, times(ONE)).expire(eq(RIDER_ID), eq(BroadcastServiceConstants.DEFAULT_REDIS_CACHE_TTL), eq(TimeUnit.SECONDS));
    verify(hashOperations, times(ONE)).put(eq(RIDER_ID), eq(JOB_ID), any(RiderBroadcastRequest.class));
  }

  @Test
  void testPutForManualExpiry() {
    redisCacheService.put(RIDER_ID, JOB_ID, RiderBroadcastRequest.builder().jobId(JOB_ID).build(), EXPIRY_TIME);
    verify(redisTemplate, times(ONE)).expire(eq(RIDER_ID), eq(EXPIRY_TIME), eq(TimeUnit.SECONDS));
    verify(hashOperations, times(ONE)).put(eq(RIDER_ID), eq(JOB_ID), any(RiderBroadcastRequest.class));
  }

  @Test
  void testValues() {
    List<Object> values = Arrays.asList(RiderBroadcastRequest.builder().jobId(JOB_ID).build());
    when(hashOperations.values(eq(RIDER_ID))).thenReturn(values);
    List<Object> result = redisCacheService.values(RIDER_ID);
    Assertions.assertEquals(JOB_ID, ((RiderBroadcastRequest) result.get(0)).getJobId());
  }

  @Test
  void testGet() {
    Object value = RiderBroadcastRequest.builder().jobId(JOB_ID).build();
    when(hashOperations.get(eq(RIDER_ID), eq(JOB_ID))).thenReturn(value);
    Object result = redisCacheService.get(RIDER_ID, JOB_ID);
    Assertions.assertEquals(JOB_ID, ((RiderBroadcastRequest) result).getJobId());
  }

  @Test
  void testSize() {
    when(hashOperations.size(eq(RIDER_ID))).thenReturn(1l);
    Object result = redisCacheService.size(RIDER_ID);
    Assertions.assertEquals(1l, result);
  }

  @Test
  void testDeleteAll() {
    redisCacheService.deleteAll(RIDER_ID);
    verify(redisTemplate, times(ONE)).delete(eq(RIDER_ID));
  }

  @Test
  void testDelete() {
    redisCacheService.delete(RIDER_ID, JOB_ID);
    verify(hashOperations, times(ONE)).delete(eq(RIDER_ID), any());
  }
}
