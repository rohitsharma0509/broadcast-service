package com.scb.rider.broadcast.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.util.ResourceUtils;

@Configuration
@Slf4j
@Profile("!local & !test")
public class MongoConfig extends AbstractMongoClientConfiguration {

  @Value("${mongo.dbName}")
  private String dbName;

  @Value("${secretsPath}")
  private String secretsPath;

  @Override
  protected String getDatabaseName() {
    return dbName;
  }

  @SneakyThrows
  @Override
  @Bean
  public MongoClient mongoClient() {
    final URI mongoUriPath = ResourceUtils.getURL(secretsPath + "/MONGO_CLUSTER_URL").toURI();
    final URI mongoUserPath = ResourceUtils.getURL(secretsPath + "/MONGO_USERNAME").toURI();
    final URI mongopassPath = ResourceUtils.getURL(secretsPath + "/MONGO_PASSWORD").toURI();

    final String mongoUri = sanitize(Files.readAllBytes(Paths.get(mongoUriPath)));
    final String mongoUser = sanitize(Files.readAllBytes(Paths.get(mongoUserPath)));
    final String mongopass = sanitize(Files.readAllBytes(Paths.get(mongopassPath)));

    String url = "mongodb://" + mongoUser + ":" + mongopass + "@" + mongoUri;
    ConnectionString connectionString = new ConnectionString(url);

    MongoClientSettings mongoClientSettings =
        MongoClientSettings.builder().applyConnectionString(connectionString).build();
    log.info("Connecting to MongoDB");
    return MongoClients.create(mongoClientSettings);
  }

  @Override
  public Collection getMappingBasePackages() {
    return Collections.singleton("com.scb.job");
  }

  private String sanitize(byte[] strBytes) {
    return new String(strBytes).replace("\r", "").replace("\n", "");
  }

  @Override
  public boolean autoIndexCreation() {
    return true;
  }

}
