package com.scb.rider.broadcast.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.scb.rider.broadcast.model.response.ConfigData;
import com.scb.rider.broadcast.service.proxy.OperationServiceProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class OperationServiceProxyTest {

  private OperationServiceProxy operationServiceProxy;

  private String opsServicePath = "http://localhost:8080";

  @Mock
  private RestTemplate restTemplate;

  @BeforeEach
  public void setup(){
    operationServiceProxy = new OperationServiceProxy(opsServicePath, 100,
        30, 3, restTemplate);

  }

  @Test
  public void testGetJobTimerConfigFromCache(){
    when(restTemplate.getForEntity(opsServicePath+"/ops/config/jobTimeout", ConfigData.class)).
        thenReturn(ResponseEntity.ok(ConfigData.builder().value(1).build()))
        .thenReturn(ResponseEntity.ok(ConfigData.builder().value(2).build()));

    assertEquals(1, operationServiceProxy.getJobTimerConfigFromCache());
    assertEquals(2, operationServiceProxy.getJobTimerConfigFromCache());

  }

  @Test
  public void testGetJobRiderTimerConfigFromCache(){
    when(restTemplate.getForEntity(opsServicePath+"/ops/config/jobTimerForRider", ConfigData.class)).
        thenReturn(ResponseEntity.ok(ConfigData.builder().value(3).build()))
        .thenReturn(ResponseEntity.ok(ConfigData.builder().value(4).build()));

    assertEquals(3, operationServiceProxy.getJobRiderTimerConfigFromCache());
    assertEquals(4, operationServiceProxy.getJobRiderTimerConfigFromCache());

  }

  @Test
  public void testGetBothDatFromCache(){
    when(restTemplate.getForEntity(opsServicePath+"/ops/config/jobTimeout", ConfigData.class)).
        thenReturn(ResponseEntity.ok(ConfigData.builder().value(1).build()))
        .thenReturn(ResponseEntity.ok(ConfigData.builder().value(2).build()));
    when(restTemplate.getForEntity(opsServicePath+"/ops/config/jobTimerForRider", ConfigData.class)).
        thenReturn(ResponseEntity.ok(ConfigData.builder().value(3).build()))
        .thenReturn(ResponseEntity.ok(ConfigData.builder().value(4).build()));

    assertEquals(1, operationServiceProxy.getJobTimerConfigFromCache());
    assertEquals(3, operationServiceProxy.getJobRiderTimerConfigFromCache());
    assertEquals(2, operationServiceProxy.getJobTimerConfigFromCache());
    assertEquals(4, operationServiceProxy.getJobRiderTimerConfigFromCache());

  }

  @Test
  public void testDefaultValueGetJobRiderTimerConfigFromCache(){
    doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST))
        .when(restTemplate).getForEntity(opsServicePath+"/ops/config/jobTimeout", ConfigData.class);
    doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST))
        .when(restTemplate).getForEntity(opsServicePath+"/ops/config/jobTimerForRider", ConfigData.class);

    assertEquals(100, operationServiceProxy.getJobTimerConfigFromCache());
    assertEquals(30, operationServiceProxy.getJobRiderTimerConfigFromCache());

  }

  @Test
  public void testGetBothDatFromCacheWithCacheTimeout() throws InterruptedException {
    when(restTemplate.getForEntity(opsServicePath+"/ops/config/jobTimeout", ConfigData.class)).
        thenReturn(ResponseEntity.ok(ConfigData.builder().value(1).build()))
        .thenReturn(ResponseEntity.ok(ConfigData.builder().value(2).build()));
    when(restTemplate.getForEntity(opsServicePath+"/ops/config/jobTimerForRider", ConfigData.class)).
        thenReturn(ResponseEntity.ok(ConfigData.builder().value(3).build()))
        .thenReturn(ResponseEntity.ok(ConfigData.builder().value(4).build()));

    assertEquals(1, operationServiceProxy.getJobTimerConfigFromCache());
    assertEquals(3, operationServiceProxy.getJobRiderTimerConfigFromCache());
    Thread.sleep(3000);
    assertEquals(2, operationServiceProxy.getJobTimerConfigFromCache());
    assertEquals(4, operationServiceProxy.getJobRiderTimerConfigFromCache());

  }


}
