package com.scb.rider.broadcast.kafka.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scb.rider.broadcast.exception.NotificationPublishException;
import com.scb.rider.broadcast.model.kafka.JobBroadcastNotification;

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
 class NotificationPublisherTest {

  @Mock
  private KafkaTemplate<String, JobBroadcastNotification> kafkaTemplate;

  private NotificationPublisher notificationPublisher;

  @BeforeEach
  public void setup(){
    notificationPublisher = new NotificationPublisher(kafkaTemplate, "topic");
  }

  @Test
  void testSendNotification()
  {
    JobBroadcastNotification jobBroadcastNotification = JobBroadcastNotification.builder().payload("android payload").build();
    ListenableFuture mockFuture = Mockito.mock(ListenableFuture.class);
    when(kafkaTemplate.send(any(Message.class))).thenReturn(mockFuture);
    notificationPublisher.send(jobBroadcastNotification);
    verify(kafkaTemplate, times(1))
        .send(any(Message.class));
  }
}
