package com.scb.rider.broadcast.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {


   @Override
   protected void configure(HttpSecurity http) throws Exception {
      http.headers().frameOptions();
      http.csrf().disable();
      http.addFilterBefore(new RequestValidationFilter(), BasicAuthenticationFilter.class);
      http.authorizeRequests().antMatchers("/").permitAll();
   }

}