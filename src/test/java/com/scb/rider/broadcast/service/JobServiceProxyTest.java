package com.scb.rider.broadcast.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.broadcast.exception.JobServiceException;
import com.scb.rider.broadcast.model.response.JobEntity;
import com.scb.rider.broadcast.model.response.JobLocation;
import com.scb.rider.broadcast.service.proxy.JobServiceProxy;
import org.junit.Assert;
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

import java.net.HttpRetryException;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JobServiceProxyTest {
    private String jobServicePath = "http://broadcast-service.default/broadcast";

    @InjectMocks
    private JobServiceProxy jobServiceProxy;

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    private String jobId = "123";
    @BeforeEach
    void setup() {
        jobServiceProxy = new JobServiceProxy(restTemplate, objectMapper, jobServicePath);
    }

    private JobEntity getJobEntity(){
        List<JobLocation> jobLocationList = new ArrayList<>();
        JobLocation jobLocation = JobLocation.builder()
                                    .subDistrict("Lat Phrao")
                                    .build();
        jobLocationList.add(jobLocation);
        return JobEntity.builder().locationList(jobLocationList).build();
    }

    @Test
    void testgetSubDistrictSuccess(){

        when(restTemplate.getForEntity(jobServicePath.concat("/job/").concat(jobId), JobEntity.class)).thenReturn(new ResponseEntity<>(getJobEntity(), HttpStatus.OK));

        JobEntity jobEntity = jobServiceProxy.getSubDistrict(jobId);
        Assert.assertEquals("Lat Phrao", jobEntity.getLocationList().get(0).getSubDistrict());
    }

    @Test
    void testShouldThrowBadRequestException() {

        String errorResponse = "{\"errorMessage\":\"Failure\"}";

        when(restTemplate.getForEntity(jobServicePath.concat("/job/").concat(jobId), JobEntity.class)).
                thenThrow(
                        new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error",
                                errorResponse.getBytes(),
                                StandardCharsets.UTF_8));
        JobServiceException jobServiceException = assertThrows(
                JobServiceException.class, () -> jobServiceProxy.getSubDistrict(jobId));

        assertEquals("Failure", jobServiceException.getErrorMessage());

    }
    @Test
    void testRetry() {

        String errorResponse = "{\"errorMessage\":\"Failure\"}";

        when(restTemplate.getForEntity(anyString(), any())).
                thenThrow(
                        new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error",
                                errorResponse.getBytes(),
                                StandardCharsets.UTF_8));
        JobServiceException jobServiceException = assertThrows(
                JobServiceException.class, () -> jobServiceProxy.findJobWithRetry(jobId));
        assertEquals("Failure", jobServiceException.getErrorMessage());

    }

    @Test
    void testFindJobSuccess() {

        when(restTemplate.getForEntity(jobServicePath.concat("/job/").concat(jobId), JobEntity.class))
                .thenReturn(new ResponseEntity<>(getJobEntity(), HttpStatus.OK));
        JobEntity jobEntity = jobServiceProxy.findJobWithRetry(jobId);
        assertEquals("Lat Phrao", jobEntity.getLocationList().get(0).getSubDistrict());

    }
    @Test
    void testRecovery() {
        JobServiceException jobServiceException = assertThrows(
                JobServiceException.class, () -> jobServiceProxy.recover(new JobServiceException("Error")));
        assertEquals("Data not found for the jobId with errorError", jobServiceException.getErrorMessage());

    }
}
