package com.scb.rider.broadcast.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
 class RestExceptionHandlerTest {

  private RestExceptionHandler restExceptionHandler = new RestExceptionHandler();

  @Test
   void testJobNotFoundException(){
    WebRequest webRequest = Mockito.mock(WebRequest.class);

    ResponseEntity<ErrorResponse> errorResponseResponseEntity =
        restExceptionHandler.handleJobNotFoundException(new JobNotFoundException("job not found")
            ,webRequest);

    assertEquals("job not found",
        errorResponseResponseEntity.getBody().getErrorMessage());

  }

}
