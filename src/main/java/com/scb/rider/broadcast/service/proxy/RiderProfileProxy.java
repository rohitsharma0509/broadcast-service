package com.scb.rider.broadcast.service.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.broadcast.exception.ErrorResponse;
import com.scb.rider.broadcast.exception.RiderProfileServiceException;
import com.scb.rider.broadcast.model.enums.RiderProfileFilters;
import com.scb.rider.broadcast.model.response.RiderDeviceDetailsResponse;
import com.scb.rider.broadcast.model.response.RiderJobDetailsResponse;
import com.scb.rider.broadcast.model.response.RiderProfileDetails;
import com.scb.rider.broadcast.model.response.RiderProfileResponse;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class RiderProfileProxy {

  public static final String FILTERS = "filters";

  private RestTemplate restTemplate;
  private String riderProfilePath;
  private ObjectMapper objectMapper;

  @Autowired
  public RiderProfileProxy(
      RestTemplate restTemplate,
      ObjectMapper objectMapper,
      @Value("${riderProfile.path}") String riderProfilePath) {
    this.restTemplate = restTemplate;
    this.riderProfilePath = riderProfilePath;
    this.objectMapper = objectMapper;
  }

  public RiderProfileResponse getRiderProfileDetails(String riderId) {

    String uri = riderProfilePath.concat("details/").concat(riderId);
    UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .fromHttpUrl(uri)
            .queryParam(FILTERS,
                    new String[]{RiderProfileFilters.RIDER_PROFILE.name()
                            , RiderProfileFilters.RIDER_DEVICE_DETAILS.name()});

    log.info("Invoking get rider profile details api:{}", uri);
    try {

      ResponseEntity<RiderProfileResponse> responseEntity =
          restTemplate.getForEntity(uriBuilder.toUriString(), RiderProfileResponse.class);
      log.info("Api invocation successful");
      return responseEntity.getBody();
    } catch (HttpClientErrorException | HttpServerErrorException ex) {
      log.error(
          "Api request error; ErrorCode:{} ; Message:{}",
          ex.getStatusCode(),
          ex.getResponseBodyAsString());
      ErrorResponse error = parseErrorResponse(ex.getResponseBodyAsString());
      if(error == null)
        throw new RiderProfileServiceException("Unknown Exception Occurred in Rider Profile Service");
      throw new RiderProfileServiceException(error.getErrorMessage());
    }
  }

  public RiderProfileDetails getRiderDetailsById(String riderId) {
    String uri = riderProfilePath.concat(riderId);
    log.info("Invoking get rider profile details api:{}", uri);
    try {
      ResponseEntity<RiderProfileDetails> responseEntity =
              restTemplate.getForEntity(uri, RiderProfileDetails.class);
      log.info("Api invocation successful");
      return responseEntity.getBody();
    } catch (HttpClientErrorException | HttpServerErrorException ex) {
      log.error(
              "Api request error; ErrorCode:{} ; Message:{}",
              ex.getStatusCode(),
              ex.getResponseBodyAsString());
      ErrorResponse error = parseErrorResponse(ex.getResponseBodyAsString());
      throw new RiderProfileServiceException(error.getErrorMessage());
    }
  }

  public RiderDeviceDetailsResponse getRiderDeviceDetails(String riderId) {
    String uri = riderProfilePath.concat(riderId).concat("/device");
    log.info("Invoking get rider device details api:{}", uri);
    try {
      ResponseEntity<RiderDeviceDetailsResponse> responseEntity =
          restTemplate.getForEntity(uri, RiderDeviceDetailsResponse.class);
      log.info("Api invocation successful");
      return responseEntity.getBody();
    } catch (HttpClientErrorException | HttpServerErrorException ex) {
      log.error(
          "Api request error; ErrorCode:{} ; Message:{}",
          ex.getStatusCode(),
          ex.getResponseBodyAsString());
      ErrorResponse error = parseErrorResponse(ex.getResponseBodyAsString());
      if(error == null)
        throw new RiderProfileServiceException("Unknown Exception Occurred in Rider Profile Service");
      throw new RiderProfileServiceException(error.getErrorMessage());
    }
  }

  public Optional<RiderJobDetailsResponse> getRiderJobDetails(String jobId) {
    String uri = riderProfilePath.concat("job/").concat(jobId);
    log.info("Invoking get job status api:{}", uri);
    try {
      ResponseEntity<RiderJobDetailsResponse> responseEntity =
          restTemplate.getForEntity(uri, RiderJobDetailsResponse.class);
      log.info("Api invocation successful");
      return Optional.ofNullable(responseEntity.getBody());
    } catch (HttpClientErrorException | HttpServerErrorException ex) {
      if(ex.getStatusCode() == HttpStatus.NOT_FOUND){
        return Optional.empty();
      }
      log.error(
          "Api request error; ErrorCode:{} ; Message:{}",
          ex.getStatusCode(),
          ex.getResponseBodyAsString());
      ErrorResponse error = parseErrorResponse(ex.getResponseBodyAsString());
      if(error == null)
        throw new RiderProfileServiceException("Unknown Exception Occurred in Rider Profile Service");
      throw new RiderProfileServiceException(error.getErrorMessage());
    }
  }

  @SneakyThrows
  private ErrorResponse parseErrorResponse(String errorResponse) {
    return objectMapper.readValue(errorResponse, ErrorResponse.class);
  }
}
