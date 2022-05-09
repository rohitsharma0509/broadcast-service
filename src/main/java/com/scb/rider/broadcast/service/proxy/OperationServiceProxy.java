package com.scb.rider.broadcast.service.proxy;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.scb.rider.broadcast.model.response.ConfigData;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class OperationServiceProxy {

  private String opsServiceUrl;

  private int defaultJobTimer;

  private int defaultJobRiderTimer;

  private RestTemplate restTemplate;

  private int cacheAge;

  private LoadingCache<String, ConfigData> jobTimerConfigCache;

  private static final String CACHE_KEY_JOB_TIMER = "jobTimeout";
  private static final String CACHE_KEY_JOB_RIDER_TIMER = "jobTimerForRider";

  @Autowired
  public OperationServiceProxy(
      @Value("${opsService.path}") String opsServiceUrl,
      @Value("${broadcast.job.time-in-seconds}") int defaultJobTimer,
      @Value("${broadcast.rider.time-in-seconds}") int defaultJobRiderTimer,
      @Value("${opsService.cacheAge}") int cacheAge,
      RestTemplate restTemplate) {
    this.opsServiceUrl = opsServiceUrl;
    this.defaultJobTimer = defaultJobTimer;
    this.defaultJobRiderTimer = defaultJobRiderTimer;
    this.restTemplate = restTemplate;
    this.cacheAge = cacheAge;
    jobTimerConfigCache = CacheBuilder.newBuilder()
        .maximumSize(2)
        .expireAfterAccess(cacheAge, TimeUnit.SECONDS)
        .build(
            new CacheLoader<String, ConfigData>() {
              public ConfigData load(String id) {
                ConfigData configData = null;
                if (id.equals(CACHE_KEY_JOB_TIMER)) {
                  configData = getJobTimerConfig();
                }else{
                  configData = getJobRiderTimerConfig();
                }
                return configData;
              }
            }
        );
  }


  @SneakyThrows
  public int getJobTimerConfigFromCache() {
    int value = getJobTimerConfig().getValue();
    log.info("Job timer config is :{}", value);
    return value;
    //int value = jobTimerConfigCache.get(CACHE_KEY_JOB_TIMER).getValue();
    //log.info("Job timer config is :{}", value);
    //return value;

  }

  @SneakyThrows
  public int getJobRiderTimerConfigFromCache() {
    int value = getJobRiderTimerConfig().getValue();
    log.info("Job Rider timer config is :{}", value);
    return value;
    //int value = jobTimerConfigCache.get(CACHE_KEY_JOB_RIDER_TIMER).getValue();
    //log.info("Job Rider timer config is :{}", value);
    //return value;

  }

  private ConfigData getJobTimerConfig(){
    String url = opsServiceUrl+ "/ops/config/jobTimeout";
    log.info("Invoking api:{}", url);
    try {
      ResponseEntity<ConfigData> configDataResponseEntity = restTemplate
          .getForEntity(url, ConfigData.class);
      ConfigData data = configDataResponseEntity.getBody();
      log.info("Returned config data:{}", data);
      return data;
    }catch (HttpClientErrorException | HttpServerErrorException ex) {
      log.error("Failed to call ops service to get JobTimer config. Reverting to default config");
      return ConfigData.builder().value(defaultJobTimer).build();
    }

  }

  private ConfigData getJobRiderTimerConfig(){
    String url = opsServiceUrl+ "/ops/config/jobTimerForRider";
    log.info("Invoking api:{}", url);
    try {
      ResponseEntity<ConfigData> configDataResponseEntity = restTemplate
          .getForEntity(url, ConfigData.class);
      ConfigData data = configDataResponseEntity.getBody();
      log.info("Returned config data:{}", data);
      return data;
    }catch (HttpClientErrorException | HttpServerErrorException ex) {
      log.error("Failed to call ops service to get JobRiderTimer config. Reverting to default config");
      return ConfigData.builder().value(defaultJobRiderTimer).build();
    }

  }

}
