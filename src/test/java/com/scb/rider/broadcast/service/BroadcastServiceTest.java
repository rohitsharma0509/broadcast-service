package com.scb.rider.broadcast.service;

import static com.scb.rider.broadcast.constants.BroadcastServiceConstants.ORDER_CANCELLED_BY_OPERATOR;
import static com.scb.rider.broadcast.constants.BroadcastStatus.BROADCASTING;
import static com.scb.rider.broadcast.util.BroadcastServiceUtils.getListOfJobs;
import static com.scb.rider.broadcast.util.BroadcastServiceUtils.getRiderProfileResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.broadcast.constants.BroadcastServiceConstants;
import com.scb.rider.broadcast.entity.BroadcastEntity;
import com.scb.rider.broadcast.entity.RiderEntity;
import com.scb.rider.broadcast.entity.redis.RiderJobEntity;
import com.scb.rider.broadcast.exception.JobNotFoundException;
import com.scb.rider.broadcast.kafka.producer.NotificationPublisher;
import com.scb.rider.broadcast.kafka.producer.RiderStatusPublisher;
import com.scb.rider.broadcast.model.JobCountAggregationResult;
import com.scb.rider.broadcast.model.kafka.JobBroadcastNotification;
import com.scb.rider.broadcast.model.kafka.RiderBroadcastRequest;
import com.scb.rider.broadcast.model.kafka.RiderStatus;
import com.scb.rider.broadcast.model.request.BroadcastRequest;
import com.scb.rider.broadcast.model.response.BroadcastJobResponse;
import com.scb.rider.broadcast.model.response.JobEntity;
import com.scb.rider.broadcast.model.response.JobLocation;
import com.scb.rider.broadcast.model.response.RiderDeviceDetailsResponse;
import com.scb.rider.broadcast.model.response.RiderJobDetailsResponse;
import com.scb.rider.broadcast.repository.BroadcastRepository;
import com.scb.rider.broadcast.repository.redis.RiderJobAcceptedRepository;
import com.scb.rider.broadcast.repository.redis.RiderJobRepository;
import com.scb.rider.broadcast.service.proxy.JobServiceProxy;
import com.scb.rider.broadcast.service.proxy.OperationServiceProxy;
import com.scb.rider.broadcast.service.proxy.RiderProfileProxy;
import com.scb.rider.broadcast.util.BroadcastServiceUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.commons.collections4.map.SingletonMap;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BroadcastServiceTest {



  @Mock BroadcastRepository broadcastRepository;
  @Mock RiderStatusPublisher riderStatusPublisher;
  @Mock MongoTemplate mongoTemplate;
  @Mock RiderProfileProxy riderProfileProxy;
  @Mock NotificationPublisher notificationPublisher;
  @Mock OperationServiceProxy operationServiceProxy;
  @Mock JobServiceProxy jobServiceProxy;
  @Mock RiderJobCacheService riderJobCacheService;
  @Mock RiderJobAcceptedRepository riderJobAcceptedRepository;

  private MeterRegistry meterRegistry = new SimpleMeterRegistry();
  private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks
  private BroadcastService broadcastService =
      new BroadcastService(
          broadcastRepository,
          riderProfileProxy,
          notificationPublisher,
          mongoTemplate,
          objectMapper,operationServiceProxy, jobServiceProxy, riderJobCacheService, riderJobAcceptedRepository,
              riderStatusPublisher, meterRegistry);

  @BeforeEach
  void setup() {
    broadcastService =
        new BroadcastService(
            broadcastRepository,
            riderProfileProxy,
            notificationPublisher,
            mongoTemplate,
            objectMapper, operationServiceProxy, jobServiceProxy, riderJobCacheService, riderJobAcceptedRepository,
                riderStatusPublisher, meterRegistry);

  }

  // testing the scenario when one rider is in rider list and is available
  @Test
  void testNewBroadcastWhenOneRiderIsProvidedWithoutCache() {
    BroadcastRequest broadcastRequest = BroadcastServiceUtils.getBroadcastRequest(1);
    RiderDeviceDetailsResponse riderDeviceDetailsResponse =
        BroadcastServiceUtils.getRiderDeviceDetailsResponse(
            broadcastRequest.getRiders().get(0).getRiderId());

    JobEntity jobEntity = getJobEntityResponse();

    when(jobServiceProxy.findJobWithRetry(anyString())).thenReturn(jobEntity);
    when(mongoTemplate.find(any(Query.class), refEq(BroadcastEntity.class)))
        .thenReturn(getListOfJobs(1));

    when(riderProfileProxy.getRiderProfileDetails(broadcastRequest.getRiders().get(0).getRiderId()))
        .thenReturn(getRiderProfileResponse(broadcastRequest.getRiders().get(0).getRiderId(), "available"));

    when(riderProfileProxy.getRiderJobDetails("RBH201205123456"))
        .thenReturn(Optional.empty());

    when(broadcastRepository.findJobCountForRider(anyString(), anyString(), any(LocalDateTime.class)))
            .thenReturn(new AggregationResults<>(List.of(JobCountAggregationResult.builder().jobCount(0).build()), new Document()));

    broadcastService.newBroadcast(broadcastRequest);
    // checking if the job has been broadcasted to the rider

    verify(notificationPublisher, times(1)).send(any(JobBroadcastNotification.class));
  }

  @Test
  void testNewBroadcastWhenOneRiderIsProvidedWithCache() {
    BroadcastRequest broadcastRequest = BroadcastServiceUtils.getBroadcastRequest(1);
    RiderDeviceDetailsResponse riderDeviceDetailsResponse =
            BroadcastServiceUtils.getRiderDeviceDetailsResponse(
                    broadcastRequest.getRiders().get(0).getRiderId());

    JobEntity jobEntity = getJobEntityResponse();

    when(jobServiceProxy.findJobWithRetry(anyString())).thenReturn(jobEntity);
    when(mongoTemplate.find(any(Query.class), refEq(BroadcastEntity.class)))
            .thenReturn(getListOfJobs(1));

    when(riderProfileProxy.getRiderProfileDetails(broadcastRequest.getRiders().get(0).getRiderId()))
            .thenReturn(getRiderProfileResponse(broadcastRequest.getRiders().get(0).getRiderId(), "available"));

    when(riderProfileProxy.getRiderJobDetails("RBH201205123456"))
            .thenReturn(Optional.empty());

    RiderBroadcastRequest riderBroadcastRequest = RiderBroadcastRequest.builder()
            .jobId("RBH201205123456")
            .distance(100)
            .build();

    Map<String, RiderBroadcastRequest> jobMap = new HashMap<>();
    jobMap.put("RBH201205123456", riderBroadcastRequest);

    RiderJobEntity riderJobEntity = RiderJobEntity.builder()
            .riderId("abcd")
            .jobMap(jobMap)
            .build();

    when(broadcastRepository.findJobCountForRider(anyString(), anyString(), any(LocalDateTime.class)))
            .thenReturn(new AggregationResults<>(List.of(JobCountAggregationResult.builder().jobCount(0).build()), new Document()));
    when(riderJobCacheService.getNumberOfJobsForRider("abcd")).thenReturn(1l);

    broadcastService.newBroadcast(broadcastRequest);

    //It has to be called twice once to get from cache and another get is to update the cache
    //verify(riderJobRepository, times(2)).findById("abcd");
    //Count should not be got from DB
    verify(broadcastRepository,times(0)).findJobCountForRider(anyString(), anyString(), any(LocalDateTime.class));
    // checking if the job has been broadcasted to the rider
    verify(notificationPublisher, times(1)).send(any(JobBroadcastNotification.class));
  }

  private JobEntity getJobEntityResponse() {
    List<JobLocation> jobLocationList = new ArrayList<>();
    JobLocation jobLocation1 = JobLocation.builder().seq(1).subDistrict("merchant sub-district").build();
    JobLocation jobLocation2 = JobLocation.builder().seq(2).subDistrict("customer sub-district").build();
    jobLocationList.add(jobLocation1);
    jobLocationList.add(jobLocation2);
    LocalDate localDate = LocalDate.now().plusDays(2);

    JobEntity jobEntity = JobEntity.builder().locationList(jobLocationList)
        .creationDateTime(localDate+"T15:57:11Z")
        .build();
    return jobEntity;
  }

  @Test
  void testNewBroadcastWhenJobAlreadyCancelled() {
    BroadcastRequest broadcastRequest = BroadcastServiceUtils.getBroadcastRequest(1);
    RiderDeviceDetailsResponse riderDeviceDetailsResponse =
        BroadcastServiceUtils.getRiderDeviceDetailsResponse(
            broadcastRequest.getRiders().get(0).getRiderId());

    RiderJobDetailsResponse riderJobDetailsResponse = RiderJobDetailsResponse
        .builder()
        .id("1234").jobId("RBH201205123456").jobStatus(ORDER_CANCELLED_BY_OPERATOR).build();

    when(mongoTemplate.find(any(Query.class), refEq(BroadcastEntity.class)))
        .thenReturn(getListOfJobs(1));

    when(riderProfileProxy.getRiderDeviceDetails(broadcastRequest.getRiders().get(0).getRiderId()))
        .thenReturn(riderDeviceDetailsResponse);

    when(riderProfileProxy.getRiderJobDetails("RBH201205123456"))
        .thenReturn(Optional.of(riderJobDetailsResponse));

    broadcastService.newBroadcast(broadcastRequest);
    // checking if the job has been broadcasted to the rider

    verify(broadcastRepository, times(0)).save(any(BroadcastEntity.class));
    verify(notificationPublisher, times(0)).send(any(JobBroadcastNotification.class));
  }

  // testing the scenario when one rider is in rider list and rider is not available for delivery
  @Test
  void testNewBroadcastWhenTwoRidersAreProvidedWithoutCache() {

    BroadcastRequest broadcastRequest = BroadcastServiceUtils.getBroadcastRequest(2);

    RiderDeviceDetailsResponse riderDeviceDetailsResponse =
        BroadcastServiceUtils.getRiderDeviceDetailsResponse(
            broadcastRequest.getRiders().get(0).getRiderId());

    JobEntity jobEntity = getJobEntityResponse();

    when(jobServiceProxy.findJobWithRetry(anyString())).thenReturn(jobEntity);
    when(operationServiceProxy.getJobTimerConfigFromCache()).thenReturn(30);

    when(riderProfileProxy.getRiderDeviceDetails(broadcastRequest.getRiders().get(0).getRiderId()))
        .thenReturn(riderDeviceDetailsResponse);


    when(riderProfileProxy.getRiderProfileDetails(broadcastRequest.getRiders().get(0).getRiderId()))
        .thenReturn(getRiderProfileResponse(broadcastRequest.getRiders().get(0).getRiderId(), "available"));

    when(riderProfileProxy.getRiderProfileDetails(broadcastRequest.getRiders().get(1).getRiderId()))
        .thenReturn(getRiderProfileResponse(broadcastRequest.getRiders().get(1).getRiderId(), "available"));

    when(riderProfileProxy.getRiderJobDetails("RBH201205123456"))
        .thenReturn(Optional.empty());

    when(broadcastRepository.findJobCountForRider(anyString(), anyString(), any(LocalDateTime.class)))
            .thenReturn(new AggregationResults<>(List.of(JobCountAggregationResult.builder().jobCount(0).build()), new Document()));
    //when(riderJobRepository.findById("abcd")).thenReturn(Optional.empty());
    //when(riderJobRepository.findById("efgh")).thenReturn(Optional.empty());

    broadcastService.newBroadcast(broadcastRequest);
    // job should not be broadcasted to the rider and rider status "RIDER NOT FOUND" should be sent

    verify(notificationPublisher, times(2)).send(any(JobBroadcastNotification.class));

  }

  @Test
  void testNewBroadcastWhenTwoRidersAreProvidedWithCache() {

    BroadcastRequest broadcastRequest = BroadcastServiceUtils.getBroadcastRequest(2);

    RiderDeviceDetailsResponse riderDeviceDetailsResponse =
            BroadcastServiceUtils.getRiderDeviceDetailsResponse(
                    broadcastRequest.getRiders().get(0).getRiderId());

    JobEntity jobEntity = getJobEntityResponse();

    when(jobServiceProxy.findJobWithRetry(anyString())).thenReturn(jobEntity);

    when(riderProfileProxy.getRiderDeviceDetails(broadcastRequest.getRiders().get(0).getRiderId()))
            .thenReturn(riderDeviceDetailsResponse);


    when(riderProfileProxy.getRiderProfileDetails(broadcastRequest.getRiders().get(0).getRiderId()))
            .thenReturn(getRiderProfileResponse(broadcastRequest.getRiders().get(0).getRiderId(), "available"));

    when(riderProfileProxy.getRiderProfileDetails(broadcastRequest.getRiders().get(1).getRiderId()))
            .thenReturn(getRiderProfileResponse(broadcastRequest.getRiders().get(1).getRiderId(), "available"));

    when(riderProfileProxy.getRiderJobDetails("RBH201205123456"))
            .thenReturn(Optional.empty());

    when(broadcastRepository.findJobCountForRider(anyString(), anyString(), any(LocalDateTime.class)))
            .thenReturn(new AggregationResults<>(List.of(JobCountAggregationResult.builder().jobCount(0).build()), new Document()));
    //when(riderJobRepository.findById("abcd")).thenReturn(Optional.empty());
    //when(riderJobRepository.findById("efgh")).thenReturn(Optional.empty());

    RiderBroadcastRequest riderBroadcastRequest = RiderBroadcastRequest.builder()
            .jobId("RBH201205123456")
            .distance(100)
            .build();

    Map<String, RiderBroadcastRequest> jobMap = new HashMap<>();
    jobMap.put("RBH201205123456", riderBroadcastRequest);

    RiderJobEntity riderJobEntity = RiderJobEntity.builder()
            .riderId("abcd")
            .jobMap(jobMap)
            .build();
    RiderJobEntity riderJobEntity2 = RiderJobEntity.builder()
            .riderId("efgh")
            .jobMap(jobMap)
            .build();

    when(broadcastRepository.findJobCountForRider(anyString(), anyString(), any(LocalDateTime.class)))
            .thenReturn(new AggregationResults<>(List.of(JobCountAggregationResult.builder().jobCount(0).build()), new Document()));
    when(riderJobCacheService.getNumberOfJobsForRider(anyString())).thenReturn(1l);
    //when(riderJobRepository.findById("abcd")).thenReturn(Optional.of(riderJobEntity));
    //when(riderJobRepository.findById("efgh")).thenReturn(Optional.of(riderJobEntity2));

    broadcastService.newBroadcast(broadcastRequest);

    //It has to be called twice once to get from cache and another get is to update the cache
    //verify(riderJobRepository, times(2)).findById("abcd");
    //verify(riderJobRepository, times(2)).findById("efgh");
    //Count should not be got from DB
    verify(broadcastRepository,times(0)).findJobCountForRider(anyString(), anyString(), any(LocalDateTime.class));
    // checking if the job has been broadcasted to the rider
    verify(notificationPublisher, times(2)).send(any(JobBroadcastNotification.class));
    // job should not be broadcasted to the rider and rider status "RIDER NOT FOUND" should be sent

  }

  @Test
  void testGetBroadcastedJob() {
    LocalDateTime expiry = LocalDateTime.now().plusSeconds(300);
    LocalDateTime lastBroadcast = LocalDateTime.now().minusSeconds(30);
    when(broadcastRepository.findByJobId("jobid"))
        .thenReturn(
            Optional.of(
                BroadcastEntity.builder()
                    .jobId("jobid")
                    .broadcastStatus(BROADCASTING.name())
                    .expiryTimeForBroadcasting(expiry)
                    .lastBroadcastDateTime(lastBroadcast)
                    .build()));
    BroadcastJobResponse broadcastJobResponse = broadcastService.getBroadcastedJob("jobid");
    assertEquals(BROADCASTING.name(), broadcastJobResponse.getBroadcastStatus());
    assertEquals("jobid", broadcastJobResponse.getJobId());
    assertEquals(expiry, broadcastJobResponse.getExpiryTimeForBroadcasting());
    assertEquals(lastBroadcast, broadcastJobResponse.getLastBroadcastDateTime());
  }

  @Test
  void testGetBroadcastedJobNotFound() {
    when(broadcastRepository.findByJobId("jobid")).thenReturn(Optional.empty());
    assertThrows(JobNotFoundException.class, () -> broadcastService.getBroadcastedJob("jobid"));
  }

  @Test
  void testGetJobsForRiderJobsFound() {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    ZonedDateTime time1 = ZonedDateTime.now().plus(60, ChronoUnit.SECONDS );
    ZonedDateTime time2 = time1.plus(2, ChronoUnit.SECONDS );
    String expiry = time1.format(formatter);

    RiderBroadcastRequest riderBroadcastRequest = RiderBroadcastRequest.builder()
            .jobId("RBH201205123456")
            .distance(100)
            .expiry(time2.format(formatter))
            .build();
    RiderBroadcastRequest riderBroadcastRequest2 = RiderBroadcastRequest.builder()
            .jobId("RBH201205123111")
            .expiry(expiry)
            .distance(100)
            .build();

    when(riderJobCacheService.getAllJobsForRider("abcd"))
        .thenReturn(Arrays.asList(riderBroadcastRequest, riderBroadcastRequest2));

    Optional<List<RiderBroadcastRequest>> jobs = broadcastService.getJobsForRider("abcd");

    assertEquals("RBH201205123456", jobs.get().get(0).getJobId());
    assertEquals("RBH201205123111", jobs.get().get(1).getJobId());
  }

  @Test
  void testGetJobsForRiderNoJobsFound() {

    //when(riderJobRepository.findById("abcd")).thenReturn(Optional.empty());

    Optional<List<RiderBroadcastRequest>> jobs = broadcastService.getJobsForRider("abcd");

    assertEquals(0, jobs.get().size());
  }

  @Test
  void testGetJobCountForRiderFromDb() {

    AggregationResults<JobCountAggregationResult> jobCountAggregationResults = Mockito.mock(AggregationResults.class);
    when(broadcastRepository.findJobCountForRider(anyString(), anyString(), any(LocalDateTime.class))).thenReturn(jobCountAggregationResults);

    Integer count  = broadcastService.getJobCountForRiderFromDb("abcd");
    assertEquals(0, count);
  }

  @Test
  void testJobCountForRidersForZeroJobs() {
    when(riderJobCacheService.getNumberOfJobsForRider(anyString())).thenReturn(0L);
    RiderEntity riderEntity = RiderEntity.builder()
            .riderId("riderId")
            .build();
    List<RiderEntity> riders = new ArrayList<>();
    riders.add(riderEntity);
    Map<String, Integer> map  = broadcastService.getJobCountForRiders(riders);
    assertEquals(1, map.size());
  }
  @Test
  void testJobCountForRidersForNullResponse() {
    when(riderJobCacheService.getNumberOfJobsForRider(anyString())).thenReturn(null);
    AggregationResults<JobCountAggregationResult> jobCountAggregationResults = Mockito.mock(AggregationResults.class);
    when(broadcastRepository.findJobCountForRider(anyString(), anyString(), any(LocalDateTime.class))).thenReturn(jobCountAggregationResults);
    RiderEntity riderEntity = RiderEntity.builder()
            .riderId("riderId")
            .build();
    List<RiderEntity> riders = new ArrayList<>();
    riders.add(riderEntity);
    Map<String, Integer> map  = broadcastService.getJobCountForRiders(riders);
    assertEquals(1, map.size());
  }

  @Test
  void testBroadcastWhenJobAlreadyExpired() {
    BroadcastRequest broadcastRequest = BroadcastServiceUtils.getBroadcastRequest(1);

    JobEntity jobEntity = getJobEntityResponse();
    jobEntity.setCreationDateTime("2021-07-22T12:00:00Z");

    when(jobServiceProxy.findJobWithRetry(anyString())).thenReturn(jobEntity);

    when(riderProfileProxy.getRiderProfileDetails(broadcastRequest.getRiders().get(0).getRiderId()))
            .thenReturn(getRiderProfileResponse(broadcastRequest.getRiders().get(0).getRiderId(), "available"));

    RiderBroadcastRequest riderBroadcastRequest = RiderBroadcastRequest.builder()
            .jobId("RBH201205123456")
            .distance(100)
            .build();

    Map<String, RiderBroadcastRequest> jobMap = new HashMap<>();
    jobMap.put("RBH201205123456", riderBroadcastRequest);

    broadcastService.newBroadcast(broadcastRequest);

    //Count should not be got from DB
    verify(broadcastRepository, times(0)).findJobCountForRider(anyString(), anyString(), any(LocalDateTime.class));
    // checking if the job has been broadcasted to the rider
    verify(notificationPublisher, times(0)).send(any(JobBroadcastNotification.class));
    verify(riderStatusPublisher, times(1)).send(any(RiderStatus.class));
    verify(broadcastRepository, times(0)).save(any(BroadcastEntity.class));

  }
  
  @Test
  void testNewBroadcastWhenOneRiderIsProvidedWithoutSubDistrict() {
    BroadcastRequest broadcastRequest = BroadcastServiceUtils.getBroadcastRequest(1);
    RiderDeviceDetailsResponse riderDeviceDetailsResponse =
        BroadcastServiceUtils.getRiderDeviceDetailsResponse(
            broadcastRequest.getRiders().get(0).getRiderId());

    JobEntity jobEntity = getJobEntityResponse();

    
    List<JobLocation> jobLocationList = new ArrayList<>();
    JobLocation jobLocation1 = JobLocation.builder().seq(1).build();
    JobLocation jobLocation2 = JobLocation.builder().seq(2).build();
    jobLocationList.add(jobLocation1);
    jobLocationList.add(jobLocation2);
    jobEntity.setLocationList(jobLocationList);
    
    
    when(jobServiceProxy.findJobWithRetry(anyString())).thenReturn(jobEntity);
    when(mongoTemplate.find(any(Query.class), refEq(BroadcastEntity.class)))
        .thenReturn(getListOfJobs(1));

    when(riderProfileProxy.getRiderProfileDetails(broadcastRequest.getRiders().get(0).getRiderId()))
        .thenReturn(getRiderProfileResponse(broadcastRequest.getRiders().get(0).getRiderId(), "available"));

    when(riderProfileProxy.getRiderJobDetails("RBH201205123456"))
        .thenReturn(Optional.empty());

    when(broadcastRepository.findJobCountForRider(anyString(), anyString(), any(LocalDateTime.class)))
            .thenReturn(new AggregationResults<>(List.of(JobCountAggregationResult.builder().jobCount(0).build()), new Document()));

    broadcastService.newBroadcast(broadcastRequest);
    // checking if the job has been broadcasted to the rider

    verify(riderStatusPublisher, times(1)).send(any(RiderStatus.class));
  }
  
  
}
