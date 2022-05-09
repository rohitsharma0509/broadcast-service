package com.scb.rider.broadcast.service;

import static com.scb.rider.broadcast.constants.BroadcastServiceConstants.ACTIVE_STATUS;
import static com.scb.rider.broadcast.constants.BroadcastServiceConstants.BROADCAST_JOBID_KEY;
import static com.scb.rider.broadcast.constants.BroadcastServiceConstants.ORDER_CANCELLED_BY_OPERATOR;
import static com.scb.rider.broadcast.constants.BroadcastStatus.BROADCASTING;
import static com.scb.rider.broadcast.constants.RiderBroadcastStatus.PENDING;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.scb.rider.broadcast.kafka.producer.RiderStatusPublisher;
import com.scb.rider.broadcast.model.kafka.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import com.scb.rider.broadcast.model.request.JobDetails;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.broadcast.constants.BroadcastServiceConstants;
import com.scb.rider.broadcast.constants.RiderBroadcastStatus;
import com.scb.rider.broadcast.entity.BroadcastEntity;
import com.scb.rider.broadcast.entity.Details;
import com.scb.rider.broadcast.entity.LatLongLocation;
import com.scb.rider.broadcast.entity.RiderEntity;
import com.scb.rider.broadcast.exception.JobNotFoundException;
import com.scb.rider.broadcast.kafka.producer.NotificationPublisher;
import com.scb.rider.broadcast.model.JobCountAggregationResult;
import com.scb.rider.broadcast.model.request.BroadcastRequest;
import com.scb.rider.broadcast.model.request.Location;
import com.scb.rider.broadcast.model.response.BroadcastJobResponse;
import com.scb.rider.broadcast.model.response.JobEntity;
import com.scb.rider.broadcast.model.response.JobLocation;
import com.scb.rider.broadcast.model.response.RiderDeviceDetailsResponse;
import com.scb.rider.broadcast.model.response.RiderJobDetailsResponse;
import com.scb.rider.broadcast.model.response.RiderProfileResponse;
import com.scb.rider.broadcast.repository.BroadcastRepository;
import com.scb.rider.broadcast.repository.redis.RiderJobAcceptedRepository;
import com.scb.rider.broadcast.service.proxy.JobServiceProxy;
import com.scb.rider.broadcast.service.proxy.OperationServiceProxy;
import com.scb.rider.broadcast.service.proxy.RiderProfileProxy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BroadcastService {


  private BroadcastRepository broadcastRepository;
  private RiderProfileProxy riderProfileProxy;
  private NotificationPublisher notificationPublisher;
  private final MongoTemplate mongoTemplate;
  private ObjectMapper objectMapper;
  private OperationServiceProxy operationServiceProxy;
  private JobServiceProxy jobServiceProxy;
  private RiderJobCacheService riderJobCacheService;
  private RiderJobAcceptedRepository riderJobAcceptedRepository;
  private RiderStatusPublisher riderStatusPublisher;
  private MeterRegistry  meterRegistry;
  private Timer jobFlashDelayTimer;

  private DateTimeFormatter jobCreatedDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

  private static Character colon = ':';
  private static String emptyString = "";

  @Autowired
  public BroadcastService(
      BroadcastRepository broadcastRepository,
      RiderProfileProxy riderProfileProxy, NotificationPublisher notificationPublisher,
      MongoTemplate mongoTemplate, ObjectMapper objectMapper,
      OperationServiceProxy operationServiceProxy, JobServiceProxy jobServiceProxy,
      RiderJobCacheService riderJobCacheService,
      RiderJobAcceptedRepository riderJobAcceptedRepository,
      RiderStatusPublisher riderStatusPublisher,
      MeterRegistry  meterRegistry) {
    this.broadcastRepository = broadcastRepository;
    this.riderProfileProxy = riderProfileProxy;
    this.notificationPublisher = notificationPublisher;
    this.mongoTemplate = mongoTemplate;
    this.objectMapper = objectMapper;
    this.operationServiceProxy = operationServiceProxy;
    this.jobServiceProxy = jobServiceProxy;
    this.riderJobCacheService = riderJobCacheService;
    this.riderJobAcceptedRepository = riderJobAcceptedRepository;
    this.riderStatusPublisher = riderStatusPublisher;
    this.meterRegistry = meterRegistry;

    this.jobFlashDelayTimer = Timer.builder(BroadcastServiceConstants.JOB_FLASH_DELAY)
            .register(meterRegistry);
  }

  public void newBroadcast(BroadcastRequest broadcastRequest) {

    log.info("Initiating Broadcast for jobId:{}", broadcastRequest.getJobDetails().getJobId());

    Optional<RiderJobDetailsResponse> riderJobDetailsResponse =
        riderProfileProxy.getRiderJobDetails(broadcastRequest.getJobDetails().getJobId());
    if (riderJobDetailsResponse.isPresent()
        && riderJobDetailsResponse.get().getJobStatus().equals(ORDER_CANCELLED_BY_OPERATOR)) {
      log.info("Job already cancelled. Not broadcasting for jobId:{}",
          broadcastRequest.getJobDetails().getJobId());
      return;
    }

    JobEntity jobEntity =
        jobServiceProxy.findJobWithRetry(broadcastRequest.getJobDetails().getJobId());
    List<JobLocation> jobLocations = jobEntity.getLocationList();
    // getting merchant and customer details for the job from the request
    List<Location> locationList = broadcastRequest.getJobDetails().getLocationList();
    Details merchantDetails = new Details();
    Details customerDetails = new Details();

    for (Location l : locationList) {
      if (l.getSeq() == 1) {
        merchantDetails.setAddress(l.getAddress());
        merchantDetails.setPhone(l.getContactPhone());
        merchantDetails.setName(l.getContactName());
        merchantDetails.setLocation(
            LatLongLocation.builder().latitude(l.getLat()).longitude(l.getLng()).build());
      }
      if (l.getSeq() == 2) {
        customerDetails.setAddress(l.getAddress());
        customerDetails.setPhone(l.getContactPhone());
        customerDetails.setName(l.getContactName());
        customerDetails.setLocation(
            LatLongLocation.builder().latitude(l.getLat()).longitude(l.getLng()).build());
      }
    }

    for (JobLocation jobLocation : jobLocations) {
    	
    	if(StringUtils.isEmpty(jobLocation.getSubDistrict()))
    	{
    		 log.error("No subdistrict was available for jobId-{},"
    		 		+ "therefore making job status as RIDER_NOT_FOUND",broadcastRequest.getJobDetails().getJobId());
    		    
    		 riderStatusPublisher.send(
    		              RiderStatus.builder()
    		 
    		              .jobId(broadcastRequest.getJobDetails().getJobId())
    		                      .riderId(null)
    		                      .status(BroadcastServiceConstants.RIDER_NOT_FOUND_STATUS)
    		                      .dateTime(ZonedDateTime.now().format(jobCreatedDateFormat))
    		                      .build());
    		      return;
    	}
    	
      if (jobLocation.getSeq() == 1) {
        merchantDetails.setSubDistrict(jobLocation.getSubDistrict());
      }
      if (jobLocation.getSeq() == 2) {
        customerDetails.setSubDistrict(jobLocation.getSubDistrict());
      }
    }

    LocalDateTime  jobEntityCreationDateTime = LocalDateTime.parse(jobEntity.getCreationDateTime(), jobCreatedDateFormat);
    //Added to  put metric for job flash delay
    jobFlashDelayTimer.record(LocalDateTime.now().getNano()- jobEntityCreationDateTime.getNano(), TimeUnit.NANOSECONDS);

    LocalDateTime expiryTime = calculateJobExpiryTime(jobEntity.getCreationDateTime());
    if(expiryTime.isBefore(LocalDateTime.now())){
      log.error("The job with jobId:{} is already expired with expiryTime:{} ; Not broadcasting",
          broadcastRequest.getJobDetails().getJobId(), expiryTime);
      riderStatusPublisher.send(
              RiderStatus.builder()
                      .jobId(broadcastRequest.getJobDetails().getJobId())
                      .riderId(null)
                      .status(BroadcastServiceConstants.RIDER_NOT_FOUND_STATUS)
                      .dateTime(ZonedDateTime.now().format(jobCreatedDateFormat))
                      .build());
      return;

    }

    // forming the broadcast entity for the job
    BroadcastEntity broadcastEntity =
        BroadcastEntity.builder().jobId(broadcastRequest.getJobDetails().getJobId())
            .price(broadcastRequest.getJobDetails().getNetPrice())
            .distance(broadcastRequest.getJobDetails().getTotalDistance())
            .merchantDetails(merchantDetails).customerDetails(customerDetails)
            .broadcastStatus(BROADCASTING.name())
            .expiryTimeForBroadcasting(expiryTime)
            .jobType(jobEntity.getJobTypeEnum())
            .batchCount(0).maxRidersForJob(broadcastRequest.getMaxRidersForJob())
            .maxJobsForRider(broadcastRequest.getMaxJobsForRider())
            .remark(broadcastRequest.getJobDetails().getRemark())
            .orderId(broadcastRequest.getJobDetails().getOrderId())
            .orderItems(broadcastRequest.getJobDetails().getOrderItems())
            .customerRemark(extractCustomerRemark(broadcastRequest))
            .minDistanceForJobCompletion(jobEntity.getMinDistanceForJobCompletion())
            .riderEntities(broadcastRequest.getRiders().stream()
                .map(rider -> RiderEntity.builder().broadcastStatus(PENDING.name())
                    .riderId(rider.getRiderId()).distance(rider.getDistance())
                    .jobTimer(operationServiceProxy.getJobRiderTimerConfigFromCache()).batchCount(0)
                    .build())
                .collect(Collectors.toList()))
            .build();

    // saving the broadcast entity in the database
    // broadcastRepository.save(broadcastEntity);
    // Broadcasting the job to the rider with according to the batch
    broadcastForRider(broadcastEntity, 1, false);

  }

  public void broadcastForRider(BroadcastEntity broadcastEntity, int batchCount,
      boolean isReBroadcast) {

    log.info("Request received for broadcasting the job to the riders");

    List<RiderEntity> riderEntityList = broadcastEntity.getRiderEntities();
    RiderBroadcastRequest riderBroadcastRequest = null;
    Map<String, RiderBroadcastRequest> riderBroadcastMap = new HashMap<>();

    if (riderEntityList != null && !riderEntityList.isEmpty()) {

      Map<String, Integer> riderJobCountMap = getJobCountForRiders(riderEntityList);

      LocalDateTime riderExpiryTime =
          LocalDateTime.now().plusSeconds(operationServiceProxy.getJobRiderTimerConfigFromCache());
      Iterator<RiderEntity> riderItr = riderEntityList.iterator();
      while (riderItr.hasNext()) {
        RiderEntity rider = riderItr.next();
        if (riderJobCountMap.get(rider.getRiderId()) >= broadcastEntity.getMaxJobsForRider()) {
          log.info("Job limit reached for the rider {} .Moving to the next rider for broadcasting",
              rider.getRiderId());
          continue;

        } else {
          if (rider.getBroadcastStatus().equals(PENDING.name())) {
            log.info("Broadcasting job with jobId {} to rider id {}", broadcastEntity.getJobId(),
                rider.getRiderId());
            RiderProfileResponse riderProfileDetails;
            try {
              riderProfileDetails =
                      riderProfileProxy.getRiderProfileDetails(rider.getRiderId());
            }catch (Exception ex){
              log.error("Failed to get rider details for broadcasting. Skipping rider:" + rider.getRiderId(), ex);
              continue;
            }

            if (riderProfileDetails.getRiderProfileDetails().getAvailabilityStatus()
                .equals(ACTIVE_STATUS)) {
              rider.setExpiryTimeForBroadcasting(riderExpiryTime);
              rider.setJobTimer(operationServiceProxy.getJobRiderTimerConfigFromCache());
              riderBroadcastRequest = getRiderBroadcastRequest(broadcastEntity, rider);
              try {
                notificationPublisher.send(getJobBroadcastNotification(
                    riderProfileDetails.getRiderDeviceDetails(), riderBroadcastRequest));
              } catch (Exception ex) {
                log.error("Error sending notification for rider:{}", rider.getRiderId(), ex);
                log.info("But still continuing with broadcast");
              }

              rider.setBroadcastStatus(RiderBroadcastStatus.BROADCASTING.name());
              rider.setBatchCount(batchCount);
              log.info("Saving job:{} to cache", broadcastEntity.getJobId());
              riderBroadcastMap.put(rider.getRiderId(), riderBroadcastRequest);
            } else {
              log.info("Rider:{}  is not active, removing from list", rider.getRiderId());
              riderItr.remove();
            }
          }
        }
      }


    } else {
      batchCount = 0;
    }
    broadcastEntity.setBatchCount(batchCount);
    broadcastEntity.setLastBroadcastDateTime(LocalDateTime.now());
    // updating the status for each rider in the database
    log.info("Saving broadcasting status to db for jobId:{}", broadcastEntity.getJobId());
    updateBroadcastDataAndCache(riderBroadcastMap, broadcastEntity, isReBroadcast);
    log.info("Saved to db for jobId:{}", broadcastEntity.getJobId());

  }

  private void updateRiderJobCacheData(Map<String, RiderBroadcastRequest> riderBroadcastMap) {
    log.info("Updating cache for broadcasting");
    riderBroadcastMap.forEach((riderId, riderBroadcastRequest) -> {
      riderJobCacheService.saveOrUpdateRiderCache(riderId, riderBroadcastRequest);
      log.info("Redis cache updated for riderId:{},  jobId:{}", riderId, riderBroadcastRequest.getJobId());
    });
  }


  public int getJobCountForRiderFromDb(String riderId) {
    AggregationResults<JobCountAggregationResult> jobCountAggregationResults =
        broadcastRepository.findJobCountForRider(BROADCASTING.name(), riderId, LocalDateTime.now());
    JobCountAggregationResult result = jobCountAggregationResults.getUniqueMappedResult();
    return result == null ? 0 : result.getJobCount();
  }


  public Map<String, Integer> getJobCountForRiders(List<RiderEntity> riders) {
    log.info("Fetching job counts for riders");
    Map<String, Integer> riderJobCountMap = new HashMap<>(riders.size());
    riders.forEach(riderEntity -> {
      int jobCount;
      Long noOfJobsFromCache = riderJobCacheService.getNumberOfJobsForRider(riderEntity.getRiderId());
      if (Objects.isNull(noOfJobsFromCache) || noOfJobsFromCache < 0) {
        jobCount = getJobCountForRiderFromDb(riderEntity.getRiderId());
      } else {
        jobCount = noOfJobsFromCache.intValue();
      }
      riderJobCountMap.put(riderEntity.getRiderId(), jobCount);
    });
    return riderJobCountMap;
  }

  public Optional<List<RiderBroadcastRequest>> getJobsForRider(String riderId) {
    log.info("Getting jobs for riderId:{}", riderId);
    List<RiderBroadcastRequest> cachedValues = riderJobCacheService.getAllJobsForRider(riderId);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    String currentDateTimeInString = ZonedDateTime.now().format(formatter);

    List<RiderBroadcastRequest> jobList = cachedValues.stream()
        .filter(obj -> !riderJobAcceptedRepository.existsById(obj.getJobId()))
        .filter(obj -> LocalDateTime.parse(obj.getExpiry(), formatter).isAfter(LocalDateTime.now()))
        .peek(broadcastData -> broadcastData.setCurrentDateTime(currentDateTimeInString))
        .sorted((RiderBroadcastRequest o1, RiderBroadcastRequest o2) -> o2.getExpiry()
            .compareTo(o1.getExpiry()))
        .collect(Collectors.toList());
    return Optional.of(jobList);
  }

  private String extractCustomerRemark(BroadcastRequest request) {

    JobDetails job = request.getJobDetails();
    if(ObjectUtils.isNotEmpty(job)) {
      List<Location> locations = job.getLocationList();
      if (ObjectUtils.isNotEmpty(locations)) {
        for (Location location : locations) {
          if (ObjectUtils.isNotEmpty(location.getSeq()) && location.getSeq() == 2) {
            String customerAddressName = location.getAddressName();
            if (StringUtils.isNotBlank(customerAddressName)) {
              int customerRemarkIndex = customerAddressName.lastIndexOf(colon);
              if (customerRemarkIndex != -1)
                return customerAddressName.substring(customerRemarkIndex);
            }
          }
        }
      }
    }
    return emptyString;
  }

  private RiderBroadcastRequest getRiderBroadcastRequest(BroadcastEntity broadcastEntity,
      RiderEntity riderEntity) {

    ZonedDateTime zonedDateTime =
        riderEntity.getExpiryTimeForBroadcasting().atZone(ZoneId.of("UTC"));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    String expiryTimeInString = zonedDateTime.format(formatter);

    String currentDateTimeInString = ZonedDateTime.now().format(formatter);


    return RiderBroadcastRequest.builder().jobId(broadcastEntity.getJobId())
        .jobType(broadcastEntity.getJobType())
        .merchantName(broadcastEntity.getMerchantDetails().getName())
        .merchantAddress(broadcastEntity.getMerchantDetails().getAddress())
        .merchantPhone(broadcastEntity.getMerchantDetails().getPhone())
        .merchantLocation(broadcastEntity.getMerchantDetails().getLocation())
        .merchantSubDistrict(broadcastEntity.getMerchantDetails().getSubDistrict())
        .customerName(broadcastEntity.getCustomerDetails().getName())
        .customerAddress(broadcastEntity.getCustomerDetails().getAddress())
        .customerPhone(broadcastEntity.getCustomerDetails().getPhone())
        .customerLocation(broadcastEntity.getCustomerDetails().getLocation())
        .customerSubDistrict(broadcastEntity.getCustomerDetails().getSubDistrict())
        .remark(broadcastEntity.getRemark()).price(broadcastEntity.getPrice())
        .distance(riderEntity.getDistance()).expiry(expiryTimeInString)
        .currentDateTime(currentDateTimeInString)
        .customerRemark(broadcastEntity.getCustomerRemark())
        .minDistanceForJobCompletion(broadcastEntity.getMinDistanceForJobCompletion())
        .orderId(broadcastEntity.getOrderId()).orderItems(broadcastEntity.getOrderItems())
        .jobTimer(riderEntity.getJobTimer()).title(BroadcastServiceConstants.TITLE)
        .body(String.format(BroadcastServiceConstants.BODY, riderEntity.getJobTimer()))
        .sound(BroadcastServiceConstants.SOUND).click_action(BroadcastServiceConstants.CLICK_ACTION)
        .build();
  }

  @SneakyThrows
  private JobBroadcastNotification getJobBroadcastNotification(
      RiderDeviceDetailsResponse riderDeviceDetailsResponse,
      RiderBroadcastRequest riderBroadcastRequest) {
    String payload = "";
    // checking if push notification is to be sent to Android device or IOS device
    if (riderDeviceDetailsResponse.getPlatform()
        .equalsIgnoreCase(BroadcastServiceConstants.ANDROID)) {
      payload = objectMapper.writeValueAsString(AndroidPayload.builder()
          .priority(BroadcastServiceConstants.PRIORITY).data(riderBroadcastRequest).build());

    } else if (riderDeviceDetailsResponse.getPlatform().equals(BroadcastServiceConstants.IOS)) {
      payload = objectMapper.writeValueAsString(IosPayload.builder()
          .aps(Aps.builder()
              .alert(Alert.builder().title(BroadcastServiceConstants.TITLE)
                  .body(String.format(BroadcastServiceConstants.BODY,
                      riderBroadcastRequest.getJobTimer()))
                  .build())
              .badge(1).sound(BroadcastServiceConstants.SOUND).build())
          .data(riderBroadcastRequest).build());
    }

    return JobBroadcastNotification.builder().arn(riderDeviceDetailsResponse.getArn())
        .type(BroadcastServiceConstants.TYPE).platform(riderDeviceDetailsResponse.getPlatform())
        .payload(payload).build();
  }

  public BroadcastJobResponse getBroadcastedJob(String jobId) {
    log.info("Fetching broadcast details for job: {}", jobId);
    BroadcastEntity broadcastEntity = broadcastRepository.findByJobId(jobId)
        .orElseThrow(() -> new JobNotFoundException("Job not found for id:" + jobId));

    return BroadcastJobResponse.builder().jobId(broadcastEntity.getJobId())
        .broadcastStatus(broadcastEntity.getBroadcastStatus())
        .expiryTimeForBroadcasting(broadcastEntity.getExpiryTimeForBroadcasting())
        .lastBroadcastDateTime(broadcastEntity.getLastBroadcastDateTime())
        .riderList(broadcastEntity.getRiderEntities()).build();
  }

  private void updateBroadcastDataAndCache(Map<String, RiderBroadcastRequest> riderBroadcastMap,
      BroadcastEntity broadcastEntity, boolean isRebroadcast) {
    /*
     * If flow is rebroadcast then to avoid race condition we will update the BroadcastEntity only
     * if it is in Broadcasting status. Which means that other thread has not made the job completed
     */
    if (isRebroadcast) {
      log.info("Saving job for reBroadcast jobId:{}", broadcastEntity.getJobId());
      Query query = new Query();
      Criteria criteria = Criteria.where(BroadcastServiceConstants.BROADCAST_STATUS)
          .is(BROADCASTING).and(BROADCAST_JOBID_KEY).is(broadcastEntity.getJobId());
      query.addCriteria(criteria);
      BroadcastEntity updatedEntity = mongoTemplate.findAndReplace(query, broadcastEntity);
      if (updatedEntity == null) {
        log.info("Broadcast was in completed state when applying rebroadcast for jobId:{} ; "
            + "Not updating cache", broadcastEntity.getJobId());
      } else {
        log.info("Saved to db for jobId:{} ; updating cache", broadcastEntity.getJobId());
        updateRiderJobCacheData(riderBroadcastMap);
      }
    } else {
      broadcastRepository.save(broadcastEntity);
      updateRiderJobCacheData(riderBroadcastMap);
      log.info("Saved to db for jobId:{}", broadcastEntity.getJobId());
    }
  }

  private LocalDateTime calculateJobExpiryTime(String jobCreationDateTime){
    LocalDateTime jobCreatedTime = LocalDateTime.parse(jobCreationDateTime, jobCreatedDateFormat);
    return  jobCreatedTime
        .plusSeconds(operationServiceProxy.getJobTimerConfigFromCache())
        .minusSeconds(2);//as a safety net for latency


  }
}
