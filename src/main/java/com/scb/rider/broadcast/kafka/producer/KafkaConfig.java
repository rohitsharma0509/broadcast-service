package com.scb.rider.broadcast.kafka.producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

  @Value("${kafka.notification-topic}")
  private String notificationTopic;

  @Value("${kafka.rider-status-topic}")
  private String riderStatustopic;

  @Bean
  public NewTopic riderStatustopic() {
    return TopicBuilder.name(riderStatustopic).partitions(3).replicas(3).build();
  }

  @Bean
  public NewTopic notificationTopic() {
    return TopicBuilder.name(notificationTopic).partitions(3).replicas(3).build();
  }
}
