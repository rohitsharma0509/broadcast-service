package com.scb.rider.broadcast.kafka.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.broadcast.entity.BroadcastEntity;
import com.scb.rider.broadcast.entity.RiderEntity;
import com.scb.rider.broadcast.model.kafka.RiderStatus;
import com.scb.rider.broadcast.repository.BroadcastRepository;
import com.scb.rider.broadcast.service.RiderJobCacheService;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
 class ListenerTest {

  @Mock private ObjectMapper objectMapper;

  @InjectMocks private Listener listener;

  @Mock private BroadcastRepository broadcastRepository;

  @Mock private RiderJobCacheService riderJobCacheService;

  @Test
  void testReceive() throws JsonProcessingException {
    RiderStatus riderStatus = getRiderStatus();

    when(objectMapper.readValue(
            "{\"riderId\":\"abc\",\"status\":\"JOB_ACCEPTED\"}", RiderStatus.class))
        .thenReturn(getRiderStatus());

    when(broadcastRepository.findByJobId(riderStatus.getJobId())).thenReturn(getBroadcastEntity());

    listener.receive("{\"riderId\":\"abc\",\"status\":\"JOB_ACCEPTED\"}", null);

    verify(broadcastRepository, times(1)).save(any(BroadcastEntity.class));
    verify(riderJobCacheService, times(1)).deleteAllJobsForRider(anyString());
    verify(riderJobCacheService, times(1)).deleteJobForRidersByBroadcastEntity(any());
  }

  @Test
  void testReceiveCancelled() throws JsonProcessingException {
    RiderStatus riderStatus = getRiderStatus();

    when(objectMapper.readValue(
        "{\"riderId\":\"abc\",\"status\":\"ORDER_CANCELLED_BY_OPERATOR\"}", RiderStatus.class))
        .thenReturn(getRiderStatusCancelled());

    when(broadcastRepository.findByJobId(riderStatus.getJobId())).thenReturn(getBroadcastEntity());

    listener.receive("{\"riderId\":\"abc\",\"status\":\"ORDER_CANCELLED_BY_OPERATOR\"}", null);

    verify(broadcastRepository, times(1)).save(any(BroadcastEntity.class));
    verify(riderJobCacheService, times(1)).deleteJobForRidersByBroadcastEntity(any());
  }

  @Test
  void testReceiveCancelledWhenNotBroadCasted() throws JsonProcessingException {
    RiderStatus riderStatus = getRiderStatus();

    when(objectMapper.readValue(
        "{\"riderId\":\"abc\",\"status\":\"ORDER_CANCELLED_BY_OPERATOR\"}", RiderStatus.class))
        .thenReturn(getRiderStatusCancelled());

    when(broadcastRepository.findByJobId(riderStatus.getJobId())).thenReturn(Optional.empty());

    listener.receive("{\"riderId\":\"abc\",\"status\":\"ORDER_CANCELLED_BY_OPERATOR\"}", null);

    verify(broadcastRepository, times(0)).save(any(BroadcastEntity.class));
  }

  private RiderStatus getRiderStatus() {
    return RiderStatus.builder().jobId("abcd1234").riderId("abc").status("JOB_ACCEPTED").build();
  }
  private RiderStatus getRiderStatusCancelled() {
    return RiderStatus.builder().jobId("abcd1234").riderId("abc").status("ORDER_CANCELLED_BY_OPERATOR").build();
  }

  private Optional<BroadcastEntity> getBroadcastEntity() {
    return Optional.ofNullable(
        BroadcastEntity.builder()
            .riderEntities(Arrays.asList(RiderEntity.builder().riderId("abc").build()))
            .jobId("abcd1234")
            .build());
  }
}
