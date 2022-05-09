package com.scb.rider.broadcast.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    String error = ex.getParameterName() + " parameter is missing";

    return new ResponseEntity<>(ErrorResponse.builder().errorMessage(error).build(), BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
	  logger.info("Exception: "+ex.getBindingResult().getFieldError().getDefaultMessage()); 
    return new ResponseEntity<>(
        ErrorResponse.builder()
            .errorMessage(
                ex.getBindingResult()
                    .getFieldError()
                    .getField()
                    .concat(" " + ex.getBindingResult().getFieldError().getDefaultMessage()))
            .build(),
        BAD_REQUEST);
  }

  @ExceptionHandler(javax.validation.ConstraintViolationException.class)
  protected ResponseEntity<Object> handleConstraintViolation(
      javax.validation.ConstraintViolationException ex) {
	  logger.info("Exception: "+ex.getLocalizedMessage()); 
    return new ResponseEntity<>(
        ErrorResponse.builder().errorMessage("Constraint Violation Exception").build(),
        BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    ServletWebRequest servletWebRequest = (ServletWebRequest) request;
    log.info(
        "{} to {}",
        servletWebRequest.getHttpMethod(),
        servletWebRequest.getRequest().getServletPath());
    String error = "Malformed JSON request";
    
    return new ResponseEntity<>(ErrorResponse.builder().errorMessage(error).build(), BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleNoHandlerFoundException(
      NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
	  logger.info("Exception: "+ex.getLocalizedMessage()); 
    return new ResponseEntity<>(
        ErrorResponse.builder().errorMessage("Unexpected Exception has occured").build(),
        INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler({RiderNotFoundForBroadcastingException.class})
  protected ResponseEntity<Object> handleEmptyRidersListException(
      RiderNotFoundForBroadcastingException ex, WebRequest request) {
	  logger.info("Exception: "+ex.getLocalizedMessage());
    return new ResponseEntity<>(
        ErrorResponse.builder()
            .errorMessage("Rider not found in the database for broadcasting")
            .build(),
        INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex, WebRequest request) {
	  logger.info("Exception: "+ex.getLocalizedMessage());
    return new ResponseEntity<>(
        ErrorResponse.builder().errorMessage(ex.getMessage()).build(), BAD_REQUEST);
  }

  @ExceptionHandler({RiderProfileServiceException.class})
  protected ResponseEntity<Object> handleRiderProfileServiceException(
      RiderProfileServiceException ex, WebRequest request) {
	  logger.info("Exception: "+ex.getLocalizedMessage());
    return new ResponseEntity<>(
        ErrorResponse.builder().errorMessage(ex.getErrorMessage()).build(), INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(JobNotFoundException.class)
  protected ResponseEntity<ErrorResponse> handleJobNotFoundException(
      JobNotFoundException ex, WebRequest request) {
	  logger.info("Exception: "+ex.getLocalizedMessage());
    return new ResponseEntity<>(
        ErrorResponse.builder().errorMessage(ex.getMessage()).build(), NOT_FOUND);
  }

  @ExceptionHandler({JobServiceException.class})
  protected ResponseEntity<Object> handleJobServiceException(
          JobServiceException ex, WebRequest request) {
    logger.info("Exception: "+ex.getLocalizedMessage());
    return new ResponseEntity<>(
            ErrorResponse.builder().errorMessage(ex.getErrorMessage()).build(), INTERNAL_SERVER_ERROR);
  }
}
