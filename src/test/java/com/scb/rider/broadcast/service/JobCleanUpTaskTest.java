package com.scb.rider.broadcast.service;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.scb.rider.broadcast.entity.BroadcastEntity;
import com.scb.rider.broadcast.kafka.producer.RiderStatusPublisher;
import com.scb.rider.broadcast.model.kafka.RiderStatus;
import com.scb.rider.broadcast.scheduler.JobCleanupTask;
import com.scb.rider.broadcast.util.BroadcastServiceUtils;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@ExtendWith(MockitoExtension.class)
public class JobCleanUpTaskTest {

  List<BroadcastEntity> jobsList;
  @Mock MongoTemplate mongoTemplate;
  @Mock RiderStatusPublisher riderStatusPublisher;
  @Mock Query query;
  @Mock RiderJobCacheService riderJobCacheService;
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

  @InjectMocks
  JobCleanupTask jobCleanupTask;

  @BeforeEach
  void setup()
  {
    jobsList= BroadcastServiceUtils.getListOfExpiredJobs();
    jobCleanupTask = new JobCleanupTask(jobsList,mongoTemplate,riderStatusPublisher,query, riderJobCacheService);
  }

  @Test
  void testRun()
  {
    jobCleanupTask.run();

    //verify(mongoTemplate, times(2)).updateMulti(any(Query.class),any(Update.class),refEq(BroadcastEntity.class));

    verify(riderJobCacheService, times(2)).deleteJobForRidersByBroadcastEntity(any(BroadcastEntity.class));
    verify(riderStatusPublisher, times(2)).send(any(RiderStatus.class));



  }

}
