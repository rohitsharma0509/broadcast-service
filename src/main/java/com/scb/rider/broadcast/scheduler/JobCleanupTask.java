package com.scb.rider.broadcast.scheduler;

import com.scb.rider.broadcast.constants.BroadcastServiceConstants;
import com.scb.rider.broadcast.entity.BroadcastEntity;
import com.scb.rider.broadcast.kafka.producer.RiderStatusPublisher;
import com.scb.rider.broadcast.model.kafka.RiderStatus;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.scb.rider.broadcast.service.RiderJobCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@Slf4j
public class JobCleanupTask implements Runnable {

  List<BroadcastEntity> jobsList;
  MongoTemplate mongoTemplate;
  RiderStatusPublisher riderStatusPublisher;
  Query query;
  RiderJobCacheService riderJobCacheService;
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");


  public JobCleanupTask(
          List<BroadcastEntity> jobsList,
          MongoTemplate mongoTemplate,
          RiderStatusPublisher riderStatusPublisher,
          Query query,
          RiderJobCacheService riderJobCacheService) {
    this.jobsList = jobsList;
    this.mongoTemplate = mongoTemplate;
    this.riderStatusPublisher = riderStatusPublisher;
    this.query = query;
    this.riderJobCacheService = riderJobCacheService;
  }

  @Override
  public void run() {

    log.info("CleanUp task started for {} jobs",jobsList.size());

    jobsList.forEach(job->{

      /**Update cache*/
      riderJobCacheService.deleteJobForRidersByBroadcastEntity(job);


      // have to push Kafka event rider-job-status topic with RIDER_NOT_FOUND for each expired job

      riderStatusPublisher.send(
          RiderStatus.builder()
              .jobId(job.getJobId())
              .riderId(null)
              .status(BroadcastServiceConstants.RIDER_NOT_FOUND_STATUS)
              .dateTime(ZonedDateTime.now().format(formatter))
              .build());

    });
    log.info("Job Clean up task completed");

  }
}
