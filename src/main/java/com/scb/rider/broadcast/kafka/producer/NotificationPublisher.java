package com.scb.rider.broadcast.kafka.producer;

import com.scb.rider.broadcast.model.kafka.JobBroadcastNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
@Slf4j
public class NotificationPublisher {

  private KafkaTemplate<String, JobBroadcastNotification> kafkaTemplate;

  private String topic;

  @Autowired
  public NotificationPublisher(
      KafkaTemplate<String, JobBroadcastNotification> kafkaTemplate,
      @Value("${kafka.notification-topic}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
  }

  public void send(JobBroadcastNotification data) {
    log.info("sending notification data to topic='{}'", topic);

    Message<JobBroadcastNotification> message =
        MessageBuilder.withPayload(data).setHeader(KafkaHeaders.TOPIC, topic).build();

    ListenableFuture<SendResult<String, JobBroadcastNotification>> listenableFuture = kafkaTemplate.send(message);
    listenableFuture.addCallback(callback());

  }

  private ListenableFutureCallback<? super SendResult<String, JobBroadcastNotification>> callback() {
    return new ListenableFutureCallback<>() {

      @Override
      public void onSuccess(SendResult<String, JobBroadcastNotification> result) {
        log.info("Notification message published successfully");
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Error while publishing rider notification message", ex);
      }

    };
  }
}
