package com.scb.rider.broadcast.kafka.producer;

import com.scb.rider.broadcast.exception.RiderStatusPublishException;
import com.scb.rider.broadcast.model.kafka.RiderStatus;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RiderStatusPublisher {

  private KafkaTemplate<String, RiderStatus> kafkaTemplate;

  private String topic;

  @Autowired
  public RiderStatusPublisher(
      KafkaTemplate<String, RiderStatus> kafkaTemplate,
      @Value("${kafka.rider-status-topic}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
  }

  public void send(RiderStatus data) {
    log.info("sending data to topic='{}'", topic);

    Message<RiderStatus> message =
        MessageBuilder.withPayload(data).setHeader(KafkaHeaders.TOPIC, topic).build();
    try {

      kafkaTemplate.send(message).get();
      log.info("Rider status successfully pushed to topic");

    } catch (InterruptedException | ExecutionException ex) {

      log.error("Exception has been occured while sending the Kafka message");
      throw new RiderStatusPublishException();
    }
  }
}
