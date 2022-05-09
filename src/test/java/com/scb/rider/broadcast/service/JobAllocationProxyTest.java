package com.scb.rider.broadcast.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.broadcast.exception.JobAllocationServiceException;
import com.scb.rider.broadcast.model.request.JobAllocationRequest;
import com.scb.rider.broadcast.model.request.Rider;
import com.scb.rider.broadcast.service.proxy.JobAllocationProxy;
import com.scb.rider.broadcast.util.BroadcastServiceUtils;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class JobAllocationProxyTest {

  private String jobAllocationPath = "http://job-allocation-service.default/find-riders";

  @InjectMocks private JobAllocationProxy jobAllocationProxy;

  @Mock private RestTemplate restTemplate;

  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    jobAllocationProxy = new JobAllocationProxy(restTemplate, objectMapper, jobAllocationPath);
  }

  @Test
  void testGetNextSetOfRiders() {
    JobAllocationRequest jobAllocationRequest = new JobAllocationRequest();
    jobAllocationRequest.setJobId("abcd");

    when(restTemplate.postForEntity(jobAllocationPath+"?limit="+5, jobAllocationRequest, Rider[].class))
        .thenReturn(new ResponseEntity<>(BroadcastServiceUtils.getRiders(), HttpStatus.OK));

    Rider[] riders = jobAllocationProxy.getNextSetOfRiders(jobAllocationRequest, 5);
    assertEquals(riders[0].getRiderId(), "abcd1");
  }

  @Test
  void testShouldThrowBadRequestException() {

    String errorResponse = "{\"errorMessage\":\"Failure\"}";

    String riderId = "abc";

    JobAllocationRequest jobAllocationRequest = new JobAllocationRequest();
    jobAllocationRequest.setJobId("abcd");

    when(restTemplate.postForEntity(jobAllocationPath+"?limit="+5, jobAllocationRequest, Rider[].class))
        .thenThrow(
            new HttpClientErrorException(
                HttpStatus.BAD_REQUEST, "Error", errorResponse.getBytes(), StandardCharsets.UTF_8));

    JobAllocationServiceException jobAllocationServiceException =
        assertThrows(
            JobAllocationServiceException.class,
            () -> jobAllocationProxy.getNextSetOfRiders(jobAllocationRequest, 5));

    assertEquals("Failure", jobAllocationServiceException.getErrorMessage());
  }
}
