package com.scb.rider.broadcast;

import com.scb.rider.tracing.tracer.EnableBasicTracer;
import com.scb.rider.tracing.tracer.logrequest.EnableRequestLog;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableRetry
@EnableMongoAuditing
@EnableSchedulerLock(defaultLockAtMostFor = "1m")
@EnableRequestLog
@EnableBasicTracer
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
