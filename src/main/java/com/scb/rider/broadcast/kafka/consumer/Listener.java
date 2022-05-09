package com.scb.rider.broadcast.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.broadcast.constants.BroadcastServiceConstants;
import com.scb.rider.broadcast.constants.BroadcastStatus;
import com.scb.rider.broadcast.constants.RiderBroadcastStatus;
import com.scb.rider.broadcast.entity.BroadcastEntity;
import com.scb.rider.broadcast.entity.RiderEntity;
import com.scb.rider.broadcast.entity.redis.RiderJobEntity;
import com.scb.rider.broadcast.exception.JobNotFoundException;
import com.scb.rider.broadcast.model.kafka.RiderStatus;
import com.scb.rider.broadcast.repository.BroadcastRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.scb.rider.broadcast.repository.redis.RiderJobRepository;
import com.scb.rider.broadcast.service.RiderJobCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.scb.rider.broadcast.constants.RiderBroadcastStatus.BROADCASTING;

@Component
@Slf4j
public class Listener {

  @Autowired private ObjectMapper objectMapper;

  @Autowired private BroadcastRepository broadcastRepository;

  @Autowired private RiderJobCacheService riderJobCacheService;

  @KafkaListener(topics = "${kafka.rider-status-topic}")
  public void receive(@Payload String data, @Headers MessageHeaders headers) {

    log.info("Received message on topic " + data);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {
      RiderStatus riderStatus = objectMapper.readValue(data, RiderStatus.class);
      if (riderStatus != null && riderStatus.getStatus() != null
          && riderStatus.getStatus().equals(BroadcastServiceConstants.JOB_ACCEPTED_STATUS)) {
        log.info("Processing JOB_ACCEPTED status for rider:{}", riderStatus.toString());

        // searching for the job and rider id
        Optional<BroadcastEntity> entity = broadcastRepository.findByJobId(riderStatus.getJobId());
        if (entity.isPresent()) {
          BroadcastEntity broadcastEntity = entity.get();
          broadcastEntity.setBroadcastStatus(BroadcastStatus.COMPLETED.name());
          log.info("Setting job broadcast to COMPLETED for jobId:{}", broadcastEntity.getJobId());

          for (RiderEntity rider : broadcastEntity.getRiderEntities()) {
            if (rider.getRiderId().equals(riderStatus.getRiderId())) {
              rider.setBroadcastStatus(RiderBroadcastStatus.ACCEPTED.name());
            }
          }

          // calling database to update the status in the database
          log.info("Saving to db for jobId:{}", riderStatus.getJobId());
          broadcastRepository.save(broadcastEntity);

          /*Update rider cache*/
          riderJobCacheService.deleteAllJobsForRider(riderStatus.getRiderId());
          riderJobCacheService.deleteJobForRidersByBroadcastEntity(broadcastEntity);
          log.info("Saving to db complete for jobId:{}", riderStatus.getJobId());

        } else {
          log.info("Job not found in broadcast database for job id " + riderStatus.getJobId());
          throw new JobNotFoundException();
        }
      }else if(riderStatus != null && riderStatus.getStatus() != null
          && riderStatus.getStatus().equals(BroadcastServiceConstants.ORDER_CANCELLED_BY_OPERATOR)){
        Optional<BroadcastEntity> entity = broadcastRepository.findByJobId(riderStatus.getJobId());
        if (entity.isPresent()) {
          BroadcastEntity broadcastEntity = entity.get();
          broadcastEntity.setBroadcastStatus(BroadcastStatus.CANCELLED.name());
          // calling database to update the status in the database
          broadcastRepository.save(broadcastEntity);
          riderJobCacheService.deleteJobForRidersByBroadcastEntity(broadcastEntity);
        }else {
          log.info("Job not found in broadcast database for job id:{}", riderStatus.getJobId());
          log.info("Ignoring message");
        }

      }

    } catch (JsonProcessingException e) {
      log.error("Error while converting incoming rider delivery status message");
    }catch (Exception e) {
      log.error("Error processing status message for data:"+data);
      log.error("Exception",e);
    }
  }
}
