package com.scb.rider.broadcast.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@ComponentScan("com.scb.rider.broadcast")
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {

  public Docket customImplementation() {
    return new Docket(DocumentationType.SWAGGER_2);
  }

  @Bean
  public Docket productApi() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.scb.job.controller"))
        .paths(PathSelectors.any())
        .build()
        .apiInfo(metaData());
  }

  private ApiInfo metaData() {
    return new ApiInfoBuilder()
        .title("Job Service")
        .description("\"Broadcast Service Documentation\"")
        .version("1.0.0")
        .license("Apache License Version 2.0 ")
        .licenseUrl("https://www.ps.com/licenses")
        .build();
  }

  public ApiInfo apiInfo() {
    final ApiInfoBuilder builder = new ApiInfoBuilder();
    builder
        .title("Job Service Swagger")
        .version("1.0")
        .license("(C) Copyright Test")
        .description("This API is responsible for broadcasting jobs to riders");

    return builder.build();
  }
}
