package com.scb.rider.broadcast.kafka.consumer;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.util.ResourceUtils;

@Configuration
@EnableKafka
@Slf4j
public class ListenerConfig {

  @Value("${kafka.consumerGroupId}")
  private String consumerGroupId;

  @Value("${kafka.noOfConcurrentMessage}")
  private int noOfConcurrentMessageProcessing;

  @Value("${kafka.groupInstanceId}")
  private String groupInstanceId;

  @Value("${secretsPath}")
  private String secretsPath;

  @Bean
  public Map<String, Object> consumerConfigs() {
    String bootstrapServers = null;
    try {
      URI bootStrapUrl = ResourceUtils.getURL(secretsPath + "/KAFKA_BOOTSTRAP_SERVERS").toURI();
      bootstrapServers = sanitize(Files.readAllBytes(Paths.get(bootStrapUrl)));
      log.info("Extracted kafka bootstrap server url {}",bootstrapServers);
    }
    catch (Exception e){
      log.error("Error extracting kafka url, extracting from properties",e);
    }
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
    props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, CustomCooperativeStickyAssignor.class.getName());
    props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, groupInstanceId);
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 180000);
    return props;
  }

  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    return new DefaultKafkaConsumerFactory<>(
        consumerConfigs(), new StringDeserializer(), new StringDeserializer());
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setConcurrency(noOfConcurrentMessageProcessing);
    factory.setErrorHandler(((exception, data) -> {
      log.error("Error in kafka process with Exception {} and the record is {}", exception, data);
    }));
    return factory;
  }

  private String sanitize(byte[] strBytes) {
    return new String(strBytes).replace("\r", "").replace("\n", "");
  }
}
