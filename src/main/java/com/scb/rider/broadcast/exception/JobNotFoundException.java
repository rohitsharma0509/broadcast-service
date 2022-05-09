package com.scb.rider.broadcast.exception;

public class JobNotFoundException extends RuntimeException {

  public JobNotFoundException() {}

  public JobNotFoundException(String message) {
    super(message);
  }
}
