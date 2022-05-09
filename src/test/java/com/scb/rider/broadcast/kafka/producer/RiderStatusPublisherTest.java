package com.scb.rider.broadcast.kafka.producer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scb.rider.broadcast.exception.RiderStatusPublishException;
import com.scb.rider.broadcast.model.kafka.RiderStatus;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.util.concurrent.ListenableFuture;

@ExtendWith(MockitoExtension.class)
 class RiderStatusPublisherTest {

  @Mock
  private KafkaTemplate<String, RiderStatus> kafkaTemplate;

  private RiderStatusPublisher riderStatusPublisher;

  @BeforeEach
  public void setup(){
    riderStatusPublisher = new RiderStatusPublisher(kafkaTemplate, "topic");
  }

  @Test
  void testSendNotification()
  {
    RiderStatus riderStatus = RiderStatus.builder().riderId("abcd1234").build();
    ListenableFuture mockFuture = Mockito.mock(ListenableFuture.class);
    when(kafkaTemplate.send(any(Message.class))).thenReturn(mockFuture);
    riderStatusPublisher.send(riderStatus);
    verify(kafkaTemplate, times(1))
        .send(any(Message.class));
  }

  @Test
   void testPublishFailedTest() throws ExecutionException, InterruptedException {

    RiderStatus riderStatus = RiderStatus.builder().riderId("abcd1234").build();
    ListenableFuture mockFuture = Mockito.mock(ListenableFuture.class);
    when(kafkaTemplate.send(any(Message.class))).thenReturn(mockFuture);
    doThrow(new InterruptedException()).when(mockFuture).get();
    assertThrows(RiderStatusPublishException.class,
        ()->riderStatusPublisher.send(riderStatus));
  }

}
