package com.scb.rider.broadcast.controller;

import com.scb.rider.broadcast.model.kafka.RiderBroadcastRequest;
import com.scb.rider.broadcast.model.request.BroadcastRequest;
import com.scb.rider.broadcast.model.response.BroadcastJobResponse;
import com.scb.rider.broadcast.service.BroadcastService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Slf4j
@RequestMapping(value="/broadcast")
public class BroadcastController {

  @Autowired private BroadcastService broadcastService;

  @PostMapping
  public ResponseEntity<Void> createNewBroadcast(
      @RequestBody @Valid final BroadcastRequest broadcastRequest) {

    broadcastService.newBroadcast(broadcastRequest);

    return new ResponseEntity<Void>(HttpStatus.CREATED);
  }

  @GetMapping(value="/jobs/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BroadcastJobResponse> getBroadcastData(
      @PathVariable("jobId") String jobId) {
    return ResponseEntity.ok(broadcastService.getBroadcastedJob(jobId));
  }

  @GetMapping(value="/jobs/rider/{rider-id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<RiderBroadcastRequest>> getJobsForRider(
      @PathVariable("rider-id") String riderId) {

    log.info("Request received for getting job details for rider "+riderId);
    Optional<List<RiderBroadcastRequest>> listOfJobs = broadcastService.getJobsForRider(riderId);
    return listOfJobs.map(riderBroadcastRequests -> ResponseEntity.status(HttpStatus.OK).body(riderBroadcastRequests))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList()));
  }
}
