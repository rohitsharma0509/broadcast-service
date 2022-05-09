package com.scb.rider.broadcast.service;

import com.scb.rider.broadcast.constants.BroadcastServiceConstants;
import com.scb.rider.broadcast.entity.BroadcastEntity;
import com.scb.rider.broadcast.entity.RiderEntity;
import com.scb.rider.broadcast.model.kafka.RiderBroadcastRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderJobCacheServiceTest {

  private static final String RIDER_ID = "RR00001";
  private static final String JOB_ID = "S1100000001";
  private static final int ONE = 1;

  @InjectMocks
  private RiderJobCacheService riderJobCacheService;

  @Mock
  private RedisCacheService redisCacheService;

  @Test
  void saveOrUpdateRiderCacheTest() {
    riderJobCacheService.saveOrUpdateRiderCache(RIDER_ID, RiderBroadcastRequest.builder().jobId(JOB_ID).build());
    verify(redisCacheService, times(ONE)).put(eq(getCacheKey()), eq(JOB_ID), any(RiderBroadcastRequest.class));
  }

  @Test
  void getAllJobsForRiderWhenNoJobsInCache() {
    when(redisCacheService.values(eq(getCacheKey()))).thenReturn(null);
    List<RiderBroadcastRequest> result = riderJobCacheService.getAllJobsForRider(RIDER_ID);
    Assertions.assertEquals(0, result.size());
  }

  @Test
  void getAllJobsForRiderWhenHaveJobsInCache() {
    when(redisCacheService.values(eq(getCacheKey()))).thenReturn(Arrays.asList(RiderBroadcastRequest.builder().jobId(JOB_ID).build()));
    List<RiderBroadcastRequest> result = riderJobCacheService.getAllJobsForRider(RIDER_ID);
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals(JOB_ID, result.get(0).getJobId());
  }

  @Test
  void getNumberOfJobsForRiderTest() {
    when(redisCacheService.size(eq(getCacheKey()))).thenReturn(1l);
    Long result = riderJobCacheService.getNumberOfJobsForRider(RIDER_ID);
    Assertions.assertEquals(1, result);
  }

  @Test
  void deleteAllJobsForRiderTest() {
    riderJobCacheService.deleteAllJobsForRider(RIDER_ID);
    verify(redisCacheService, times(ONE)).deleteAll(eq(getCacheKey()));
  }

  @Test
  void deleteJobForRidersByBroadcastEntityTest() {
    RiderEntity rider = RiderEntity.builder().riderId(RIDER_ID).build();
    List<RiderEntity> riders = Arrays.asList(rider);
    BroadcastEntity broadcastEntity = BroadcastEntity.builder().jobId(JOB_ID).riderEntities(riders).build();
    riderJobCacheService.deleteJobForRidersByBroadcastEntity(broadcastEntity);
    verify(redisCacheService, times(ONE)).delete(eq(getCacheKey()), eq(JOB_ID));
  }
  @Test
  void deleteJobForRidersByBroadcastEntityTestForNull() {
    riderJobCacheService.deleteJobForRidersByBroadcastEntity(null);
    verify(redisCacheService, times(0)).delete(eq(getCacheKey()), eq(JOB_ID));
  }

  @Test
  void deleteJobForRidersByBroadcastEntityTestWithEmptyRiderList() {
    BroadcastEntity broadcastEntity = BroadcastEntity.builder()
            .jobId(JOB_ID)
            .build();
    riderJobCacheService.deleteJobForRidersByBroadcastEntity(broadcastEntity);
    verify(redisCacheService, times(0)).delete(eq(getCacheKey()), eq(JOB_ID));
  }

  @Test
  void deleteJobForRidersTest() {
    riderJobCacheService.deleteJobForRiders(Arrays.asList(RIDER_ID), JOB_ID);
    verify(redisCacheService, times(ONE)).delete(eq(getCacheKey()), eq(JOB_ID));
  }

  @Test
  void deleteJobForRidersTestNull() {
    riderJobCacheService.deleteJobForRiders(null, JOB_ID);
    verify(redisCacheService, times(0)).delete(eq(getCacheKey()), eq(JOB_ID));
  }

  @Test
  void deleteJobForRiderTest() {
    riderJobCacheService.deleteJobForRider(RIDER_ID, JOB_ID);
    verify(redisCacheService, times(ONE)).delete(eq(getCacheKey()), eq(JOB_ID));
  }

  private String getCacheKey() {
    return BroadcastServiceConstants.RIDER_JOB_CACHE_KEY_PREFIX.concat(RIDER_ID);
  }
}

