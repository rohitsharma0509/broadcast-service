package com.scb.rider.broadcast.kafka.producer;

import com.scb.rider.broadcast.model.kafka.JobBroadcastNotification;
import com.scb.rider.broadcast.model.kafka.RiderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.ResourceUtils;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.CommonClientConfigs.RETRY_BACKOFF_MS_CONFIG;

@Configuration
@Slf4j
public class kafkaPublisherConfig {
  public static final String ACKS_ALL = "all";
  public static final String MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION_ONE = "1";
  public static final String LINGER_MS_ZERO = "0";
  public static final String BATCH_SIZE_VALUE = "1048576";

  @Value("${secretsPath}")
  private String secretsPath;


  Map<String, Object> defaultConfig() {

    final Map<String, Object> defaultConfigMap = new HashMap<>();

    defaultConfigMap.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
    defaultConfigMap.put(RETRY_BACKOFF_MS_CONFIG, 1000);
    defaultConfigMap.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    defaultConfigMap.put(ProducerConfig.ACKS_CONFIG, ACKS_ALL);
    defaultConfigMap.put(ProducerConfig.BATCH_SIZE_CONFIG, BATCH_SIZE_VALUE);
    defaultConfigMap.put(ProducerConfig.RETRIES_CONFIG, 10);
    defaultConfigMap.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 1000);
    defaultConfigMap.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1000);
    defaultConfigMap.put("errors.retry.delay.max.ms", 1000);

    return defaultConfigMap;
  }

  @Bean
  public Map<String, Object> producerConfigs() {
    String bootstrapServers = getBootStrapUrls();
    Map<String, Object> props = new HashMap<>(defaultConfig());
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return props;
  }

  @Bean
  public ProducerFactory<String, Object> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public KafkaAdmin admin() {
    String urls = getBootStrapUrls();
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, urls);
    return new KafkaAdmin(configs);
  }

  @Bean
  public KafkaTemplate<String, JobBroadcastNotification> kafkaTemplate() {
    return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs()));
  }
  @Bean
  public KafkaTemplate<String, RiderStatus> kafkaTemplateRiderJobStatusEventModel() {
    return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs()));
  }

  private String getBootStrapUrls(){
    String server = null;
    try {
      URI bootStrapUrl = ResourceUtils.getURL(secretsPath + "/KAFKA_BOOTSTRAP_SERVERS").toURI();
      server = sanitize(Files.readAllBytes(Paths.get(bootStrapUrl)));
      log.info("Extracted kafka bootstrap server url {}",server);
    }
    catch (Exception e){
      log.error("Error extracting kafka url",e);
      throw new RuntimeException("Error extracting kafka url");
    }
    return server;

  }

  private String sanitize(byte[] strBytes) {
    return new String(strBytes).replace("\r", "").replace("\n", "");
  }

}
