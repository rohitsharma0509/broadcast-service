package com.scb.rider.broadcast.scheduler;

import com.scb.rider.broadcast.constants.BroadcastServiceConstants;
import com.scb.rider.broadcast.entity.BroadcastEntity;
import com.scb.rider.broadcast.kafka.producer.RiderStatusPublisher;
import com.scb.rider.broadcast.service.RiderJobCacheService;
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
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.scb.rider.broadcast.constants.BroadcastStatus.*;

@Component
@Slf4j
public class JobCleanUpScheduler {


  private final MongoTemplate mongoTemplate;
  private final RiderStatusPublisher riderStatusPublisher;
  private final ExecutorService jobCleanupExecutorThreadPool;
  private final OperationServiceProxy operationServiceProxy;
  private final RiderJobCacheService riderJobCacheService;

  @Autowired
  public JobCleanUpScheduler(

      MongoTemplate mongoTemplate,
      RiderStatusPublisher riderStatusPublisher,
      @Qualifier("jobCleanupExecutorThreadPool")
      ExecutorService jobCleanupExecutorThreadPool,
      OperationServiceProxy operationServiceProxy,
      RiderJobCacheService riderJobCacheService) {

    this.mongoTemplate = mongoTemplate;
    this.riderStatusPublisher = riderStatusPublisher;
    this.jobCleanupExecutorThreadPool = jobCleanupExecutorThreadPool;
    this.operationServiceProxy = operationServiceProxy;
    this.riderJobCacheService = riderJobCacheService;
  }

  @Scheduled(fixedDelay = 2000)
  @SchedulerLock(name="jobCleanUpScheduler", lockAtMostFor = "20s")
  public void checkForExpiredJobs() {
    String jobScheduleId = UUID.randomUUID().toString();
    log.info("Scheduler Job invoked for checking the job broadcast time is expired, requestId:{}",
        jobScheduleId);

    Query query = new Query();

    Criteria criteria =
        Criteria.where(BroadcastServiceConstants.BROADCAST_STATUS).is(BROADCASTING)
            .and(BroadcastServiceConstants.EXPIRY_TIME_FOR_BROADCASTING)
            .lt(LocalDateTime.now());

    query.addCriteria(criteria);

    List<BroadcastEntity> listOfExpiredJobs = mongoTemplate.find(query, BroadcastEntity.class);

    if (listOfExpiredJobs.isEmpty()) {
      log.info("No expired job found");
    } else {

      /* updating the jobs to COMPLETED status */
      Update update = new Update();
      update.set("broadcastStatus", "COMPLETED");
      mongoTemplate.updateMulti(query, update, BroadcastEntity.class);

      log.info("Number of jobs eligible for cleanup are {} ",listOfExpiredJobs.size());
      CompletableFuture<?>[] futures =  ListUtils.partition(listOfExpiredJobs, getBatchSize(listOfExpiredJobs.size())).stream()
          .map(
              jobBatch -> CompletableFuture.runAsync( new JobCleanupTask(jobBatch, mongoTemplate, riderStatusPublisher, query, riderJobCacheService),jobCleanupExecutorThreadPool)
                     ).toArray(CompletableFuture[]::new);
      CompletableFuture.allOf(futures).join();

    }
    log.info("JobCleanUpScheduler completed, requestId:{}",jobScheduleId);
  }

  private int getBatchSize(int listSize) {
    return (int) Math.ceil((float) listSize / 10);
  }
}
