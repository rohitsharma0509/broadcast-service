package com.scb.rider.broadcast.service;



import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scb.rider.broadcast.entity.BroadcastEntity;
import com.scb.rider.broadcast.model.kafka.RiderStatus;
import com.scb.rider.broadcast.model.request.JobAllocationRequest;
import com.scb.rider.broadcast.repository.BroadcastRepository;
import com.scb.rider.broadcast.scheduler.JobReBroadcastTask;
import com.scb.rider.broadcast.service.proxy.JobAllocationProxy;
import com.scb.rider.broadcast.util.BroadcastServiceUtils;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JobReBroadcastTaskTest {

  List<BroadcastEntity> jobsList;
  @Mock BroadcastRepository broadcastRepository;
  @Mock BroadcastService broadcastService;
  @Mock JobAllocationProxy jobAllocationProxy;
  @Mock RiderJobCacheService riderJobCacheService;

  @InjectMocks JobReBroadcastTask jobReBroadcastTask;

  @BeforeEach
  void setup() {
    jobsList = BroadcastServiceUtils.getListOfExpiredJobs();
    jobReBroadcastTask =
        new JobReBroadcastTask(jobsList, broadcastRepository, broadcastService, jobAllocationProxy, riderJobCacheService);
  }

  @Test
  void testRun() {

    when(jobAllocationProxy.getNextSetOfRiders(any(JobAllocationRequest.class), any(Integer.class)))
        .thenReturn(BroadcastServiceUtils.getRiders());

    jobReBroadcastTask.run();

    verify(broadcastService, times(2)).broadcastForRider(any(BroadcastEntity.class),any(Integer.class), anyBoolean());



  }
}
