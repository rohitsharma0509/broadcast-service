package com.scb.rider.broadcast.service.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.broadcast.exception.ErrorResponse;
import com.scb.rider.broadcast.exception.JobAllocationServiceException;
import com.scb.rider.broadcast.model.request.JobAllocationRequest;
import com.scb.rider.broadcast.model.request.Rider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class JobAllocationProxy {

  private RestTemplate restTemplate;
  private String jobAllocationPath;
  private ObjectMapper objectMapper;

  @Autowired
  public JobAllocationProxy(
      RestTemplate restTemplate,
      ObjectMapper objectMapper,
      @Value("${jobAllocation.path}") String jobAllocationPath) {
    this.restTemplate = restTemplate;
    this.jobAllocationPath = jobAllocationPath;
    this.objectMapper = objectMapper;
  }


  public Rider[] getNextSetOfRiders(JobAllocationRequest jobAllocationRequest, Integer riderLimit) {
    String uri = jobAllocationPath.concat("?limit=").concat(Integer.toString(riderLimit));
    log.info("Invoking job allocation service to get the next set of riders for {}", uri);
    try {
      ResponseEntity<Rider[]> responseEntity =
          restTemplate.postForEntity(uri, jobAllocationRequest, Rider[].class);

      log.info("Job allocation Api invocation successful");
      return responseEntity.getBody();

    } catch (HttpClientErrorException | HttpServerErrorException ex) {
      log.error(
          "Api request error; ErrorCode:{} ; Message:{}",
          ex.getStatusCode(),
          ex.getResponseBodyAsString());
      ErrorResponse error = parseErrorResponse(ex.getResponseBodyAsString());
      if(error == null)
        throw new JobAllocationServiceException("Unknown Exception Occurred in Job Allocation Service");
      throw new JobAllocationServiceException(error.getErrorMessage());
    }
  }

  @SneakyThrows
  private ErrorResponse parseErrorResponse(String errorResponse) {
    return objectMapper.readValue(errorResponse, ErrorResponse.class);
  }
}
