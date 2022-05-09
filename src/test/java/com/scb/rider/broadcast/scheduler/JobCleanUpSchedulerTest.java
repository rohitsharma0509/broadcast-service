package com.scb.rider.broadcast.scheduler;

import static com.scb.rider.broadcast.util.BroadcastServiceUtils.getListOfExpiredJobs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scb.rider.broadcast.entity.BroadcastEntity;
import com.scb.rider.broadcast.kafka.producer.RiderStatusPublisher;
import java.util.concurrent.ExecutorService;

import com.scb.rider.broadcast.service.RiderJobCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class JobCleanUpSchedulerTest {

  @Mock JobCleanupTask jobCleanupTask;
  @Mock MongoTemplate mongoTemplate;
  @Mock RiderStatusPublisher riderStatusPublisher;
  @Mock ExecutorService jobCleanupExecutorThreadPool;
  @Mock RiderJobCacheService riderJobCacheService;
  @InjectMocks JobCleanUpScheduler jobCleanUpScheduler;

  @Test
  void testCheckForExpiredJobs() {

    // getting list of 2 broadcast jobs
    when(mongoTemplate.find(any(Query.class), refEq(BroadcastEntity.class)))
        .thenReturn(getListOfExpiredJobs());

     doAnswer(
         (InvocationOnMock invocation) -> {
           ((Runnable) invocation.getArguments()[0]).run();
           return null;
         }
     ).when(jobCleanupExecutorThreadPool).execute(any(Runnable.class));

    // invoking method to check for expired jobs
    jobCleanUpScheduler.checkForExpiredJobs();

    // verify that rider status is published 2 times as no of expired jobs are 2
    verify(jobCleanupExecutorThreadPool, times(2)).execute(any());
  }
}
