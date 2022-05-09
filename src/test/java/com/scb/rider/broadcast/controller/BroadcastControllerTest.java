package com.scb.rider.broadcast.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scb.rider.broadcast.model.kafka.RiderBroadcastRequest;
import com.scb.rider.broadcast.model.request.BroadcastRequest;
import com.scb.rider.broadcast.model.response.BroadcastJobResponse;
import com.scb.rider.broadcast.service.BroadcastService;
import com.scb.rider.broadcast.util.BroadcastServiceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class BroadcastControllerTest {

  @InjectMocks private BroadcastController broadcastController;

  @Mock private BroadcastService broadcastService;

  @Test
  void testCreateNewBroadcast() {
    BroadcastRequest broadcastRequest = BroadcastServiceUtils.getBroadcastRequest(1);
    ResponseEntity<Void> responseEntity = broadcastController.createNewBroadcast(broadcastRequest);
    assertEquals(201, responseEntity.getStatusCodeValue());
    verify(broadcastService, times(1)).newBroadcast(any(BroadcastRequest.class));
  }

  @Test
  void testGetBroadcastData() {
    when(broadcastService.getBroadcastedJob("jobid"))
        .thenReturn(
            BroadcastJobResponse.builder().jobId("jobid").broadcastStatus("status").build());
    broadcastController.getBroadcastData("jobid");
    verify(broadcastService, times(1)).getBroadcastedJob("jobid");
  }

  @Test
  void testGetJobsForRider() {
    List<RiderBroadcastRequest> broadcastRequestList = new ArrayList<>();
    broadcastRequestList.add(RiderBroadcastRequest.builder().jobId("abcd1234").build());

    when(broadcastService.getJobsForRider("riderId")).thenReturn(Optional.of(broadcastRequestList));

    ResponseEntity<List<RiderBroadcastRequest>> responseEntity =  broadcastController.getJobsForRider("riderId");

    assertEquals(200,responseEntity.getStatusCodeValue());
    assertEquals("abcd1234",responseEntity.getBody().get(0).getJobId());


  }
}
