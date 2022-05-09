package com.scb.rider.broadcast.service;

import static com.scb.rider.broadcast.constants.BroadcastServiceConstants.ORDER_CANCELLED_BY_OPERATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.broadcast.exception.RiderProfileServiceException;
import com.scb.rider.broadcast.model.response.RiderDeviceDetailsResponse;
import com.scb.rider.broadcast.model.response.RiderJobDetailsResponse;
import com.scb.rider.broadcast.model.response.RiderProfileDetails;
import com.scb.rider.broadcast.model.response.RiderProfileResponse;
import com.scb.rider.broadcast.service.proxy.RiderProfileProxy;
import com.scb.rider.broadcast.util.BroadcastServiceUtils;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
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
 class RiderProfileProxyTest {

  private String riderProfilePath = "http://broadcast-service.default/broadcast/";

  @InjectMocks
  private RiderProfileProxy riderProfileProxy;

  @Mock
  private RestTemplate restTemplate;

  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    riderProfileProxy = new RiderProfileProxy(restTemplate, objectMapper, riderProfilePath);
  }

  @Test
  void testGetRiderProfile()
  {
    String riderId = "abc";
    when(restTemplate.getForEntity(riderProfilePath.concat("details/").concat(riderId)
            .concat("?filters=RIDER_PROFILE&filters=RIDER_DEVICE_DETAILS"), RiderProfileResponse.class))
        .thenReturn(new ResponseEntity<>(BroadcastServiceUtils.getRiderProfileResponse(riderId,"available"), HttpStatus.OK));

    RiderProfileResponse riderProfileResponse = riderProfileProxy.getRiderProfileDetails(riderId);
    assertEquals("Active",riderProfileResponse.getRiderProfileDetails().getAvailabilityStatus());
    assertEquals("Jason",riderProfileResponse.getRiderProfileDetails().getFirstName());

  }
    @Test
    void testGetRiderProfileById()
    {
        String riderId = "abc";
        RiderProfileDetails riderProfileDetails = RiderProfileDetails.builder()
                .availabilityStatus("Active")
                .firstName("Jason")
                .riderId("abc")
                .build();
        when(restTemplate.getForEntity(riderProfilePath.concat(riderId), RiderProfileDetails.class))
                .thenReturn(new ResponseEntity<>(riderProfileDetails, HttpStatus.OK));

        RiderProfileDetails riderProfileResponse = riderProfileProxy.getRiderDetailsById(riderId);
        assertEquals("Active",riderProfileResponse.getAvailabilityStatus());
        assertEquals("Jason",riderProfileResponse.getFirstName());

    }

  @Test
  void testGetRiderDeviceDetails()
  {
    String riderId = "abc";
    when(restTemplate.getForEntity(riderProfilePath.concat(riderId).concat("/device"), RiderDeviceDetailsResponse.class))
        .thenReturn(new ResponseEntity<>(BroadcastServiceUtils.getRiderDeviceDetailsResponse(riderId), HttpStatus.OK));

    RiderDeviceDetailsResponse riderDeviceDetailsResponse = riderProfileProxy.getRiderDeviceDetails(riderId);
    assertEquals("abcd1234",riderDeviceDetailsResponse.getArn());
    assertEquals("GCM",riderDeviceDetailsResponse.getPlatform());
  }

  @Test
  void testGetRiderJobDetails()
  {
    String jobId = "abc";
    when(restTemplate.getForEntity(riderProfilePath.concat("job/").concat(jobId), RiderJobDetailsResponse.class))
        .thenReturn(new ResponseEntity<>(RiderJobDetailsResponse
            .builder().id("1234").jobId(jobId).jobStatus(ORDER_CANCELLED_BY_OPERATOR)
            .build(), HttpStatus.OK));

    Optional<RiderJobDetailsResponse> riderJobDetailsResponse = riderProfileProxy.getRiderJobDetails(jobId);
    assertEquals("1234",riderJobDetailsResponse.get().getId());
    assertEquals("abc",riderJobDetailsResponse.get().getJobId());
  }

  @Test
  void testGetRiderJobDetailsNotFound()
  {
    String jobId = "abc";
    doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
        .when(restTemplate).getForEntity(riderProfilePath.concat("job/").concat(jobId),
        RiderJobDetailsResponse.class);

    Optional<RiderJobDetailsResponse> riderJobDetailsResponse = riderProfileProxy.getRiderJobDetails(jobId);
    assertFalse(riderJobDetailsResponse.isPresent());
  }

  @Test
   void testShouldThrowBadRequestException() {

    String errorResponse = "{\"errorMessage\":\"Failure\"}";

    String riderId = "abc";

    when(restTemplate.getForEntity(riderProfilePath.concat("details/").concat(riderId)
            .concat("?filters=RIDER_PROFILE&filters=RIDER_DEVICE_DETAILS"), RiderProfileResponse.class)).
        thenThrow(
            new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error",
                errorResponse.getBytes(),
                StandardCharsets.UTF_8));
    RiderProfileServiceException riderProfileServiceException = assertThrows(
        RiderProfileServiceException.class, () -> riderProfileProxy.getRiderProfileDetails(riderId));

    assertEquals("Failure", riderProfileServiceException.getErrorMessage());

  }
    @Test
    void testThrowExceptionForGetRiderProfileCall() {

        String errorResponse = "{\"errorMessage\":\"Failure\"}";

        String riderId = "abc";

        when(restTemplate.getForEntity(riderProfilePath.concat(riderId), RiderProfileDetails.class)).
                thenThrow(
                        new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Error",
                                errorResponse.getBytes(),
                                StandardCharsets.UTF_8));
        RiderProfileServiceException riderProfileServiceException = assertThrows(
                RiderProfileServiceException.class, () -> riderProfileProxy.getRiderDetailsById(riderId));

        assertEquals("Failure", riderProfileServiceException.getErrorMessage());

    }


}
