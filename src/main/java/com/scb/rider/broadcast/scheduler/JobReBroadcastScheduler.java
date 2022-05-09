package com.scb.rider.broadcast.scheduler;

import com.scb.rider.broadcast.constants.BroadcastServiceConstants;
import com.scb.rider.broadcast.constants.BroadcastStatus;
import com.scb.rider.broadcast.entity.BroadcastEntity;
import com.scb.rider.broadcast.repository.BroadcastRepository;
import com.scb.rider.broadcast.service.BroadcastService;
import com.scb.rider.broadcast.service.RiderJobCacheService;
import com.scb.rider.broadcast.service.proxy.JobAllocationProxy;
import com.scb.rider.broadcast.service.proxy.OperationServiceProxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobReBroadcastScheduler {

  private final BroadcastService broadcastService;
  private final BroadcastRepository broadcastRepository;
  private final MongoTemplate mongoTemplate;
  private final JobAllocationProxy jobAllocationProxy;
  private final ExecutorService riderBroadcastExecutorThreadPool;
  private final OperationServiceProxy operationServiceProxy;
  private final RiderJobCacheService riderJobCacheService;

  @Autowired
  public JobReBroadcastScheduler(
      BroadcastService broadcastService,
      BroadcastRepository broadcastRepository,
      MongoTemplate mongoTemplate,
      JobAllocationProxy jobAllocationProxy,
      @Qualifier("riderBroadcastExecutorThreadPool")
      ExecutorService riderBroadcastExecutorThreadPool,
      OperationServiceProxy operationServiceProxy,
      RiderJobCacheService riderJobCacheService) {

    this.broadcastService = broadcastService;
    this.broadcastRepository = broadcastRepository;
    this.mongoTemplate = mongoTemplate;
    this.jobAllocationProxy = jobAllocationProxy;
    this.riderBroadcastExecutorThreadPool = riderBroadcastExecutorThreadPool;
    this.operationServiceProxy = operationServiceProxy;
    this.riderJobCacheService = riderJobCacheService;
  }

  @Scheduled(fixedDelay = 5000)
  @SchedulerLock(name="jobReBroadcastScheduler", lockAtMostFor = "30s")
  public void checkForExpiredRiderJob() {
    String jobScheduleId = UUID.randomUUID().toString();
    log.info("Scheduler Job invoked for checking the rider expiry time and updating the status, requestId:{}", jobScheduleId);

    Query query = new Query();
    Criteria criteria =
        Criteria.where(BroadcastServiceConstants.BROADCAST_STATUS)
            .is(BroadcastStatus.BROADCASTING.name())
            .and(BroadcastServiceConstants.LAST_BROADCAST_DATE_TIME)
            .lt(LocalDateTime.now().minusSeconds(operationServiceProxy.getJobRiderTimerConfigFromCache()))
            .and(BroadcastServiceConstants.EXPIRY_TIME_FOR_BROADCASTING)
            .gt(LocalDateTime.now());

    query.addCriteria(criteria);

    List<BroadcastEntity> broadcastEntities = mongoTemplate.find(query, BroadcastEntity.class);

    if (broadcastEntities.isEmpty()) {
      log.info("No broadcasting job found matching the criteria");
    } else {
      log.info(
          "Total number of jobs for which riders have not accepted the order are "
              + broadcastEntities.size());

      log.info("Creating job partitions");
      CompletableFuture<?>[] futures = ListUtils.partition(broadcastEntities, getBatchSize(broadcastEntities.size())).stream()
          .map(
              jobBatch -> CompletableFuture.runAsync(new JobReBroadcastTask(jobBatch,broadcastRepository,broadcastService,jobAllocationProxy, riderJobCacheService), riderBroadcastExecutorThreadPool)
          ).toArray(CompletableFuture[]::new);
      CompletableFuture.allOf(futures).join();

    }
    log.info("JobReBroadcastScheduler completed, requestId:{}", jobScheduleId);
  }

  private int getBatchSize(int listSize) {
    return (int) Math.ceil((float) listSize / 40);
  }
}
