package com.scb.rider.broadcast.scheduler;

import static com.scb.rider.broadcast.util.BroadcastServiceUtils.getListOfExpiredJobs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scb.rider.broadcast.entity.BroadcastEntity;
import com.scb.rider.broadcast.repository.BroadcastRepository;
import com.scb.rider.broadcast.service.BroadcastService;
import com.scb.rider.broadcast.service.RiderJobCacheService;
import com.scb.rider.broadcast.service.proxy.JobAllocationProxy;
import com.scb.rider.broadcast.service.proxy.OperationServiceProxy;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
public class JobReBroadcastSchedulerTest {

  @Mock JobReBroadcastTask jobReBroadcastTask;
  @Mock BroadcastRepository broadcastRepository;
  @Mock MongoTemplate mongoTemplate;
  @Mock BroadcastService broadcastService;
  @Mock JobAllocationProxy jobAllocationProxy;
  @Mock ExecutorService riderBroadcastExecutorThreadPool;
  @Mock OperationServiceProxy operationServiceProxy;
  @Mock RiderJobCacheService riderJobCacheService;
  @InjectMocks JobReBroadcastScheduler jobReBroadcastScheduler;

  @Test
  void testCheckForExpiredRiderJob() {
    // getting list of 2 broadcast jobs
    when(mongoTemplate.find(any(Query.class), refEq(BroadcastEntity.class)))
        .thenReturn(getListOfExpiredJobs());

    doAnswer(
        (InvocationOnMock invocation) -> {
          ((Runnable) invocation.getArguments()[0]).run();
          return 1;
        }
    ).when(riderBroadcastExecutorThreadPool).execute(any(Runnable.class));

    // invoking method to check for expired jobs
    jobReBroadcastScheduler.checkForExpiredRiderJob();

    // verify that rider status is published 2 times as no of expired jobs are 2
    verify(riderBroadcastExecutorThreadPool, times(2)).execute(any());
  }
}
