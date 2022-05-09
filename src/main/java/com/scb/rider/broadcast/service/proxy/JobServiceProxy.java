package com.scb.rider.broadcast.service.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.broadcast.exception.ErrorResponse;
import com.scb.rider.broadcast.exception.JobServiceException;
import com.scb.rider.broadcast.model.response.JobEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class JobServiceProxy {

    private RestTemplate restTemplate;
    private String jobServicePath;
    private ObjectMapper objectMapper;

    @Autowired
    public JobServiceProxy(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${jobService.path}") String jobServicePath) {
        this.restTemplate = restTemplate;
        this.jobServicePath = jobServicePath;
        this.objectMapper = objectMapper;
    }
    @Retryable(
            value = {JobServiceException.class},
            maxAttempts = 3,
            backoff = @Backoff(random = true, delay = 200, maxDelay = 500, multiplier = 2))
    public JobEntity findJobWithRetry(String jobID) throws JobServiceException {
        return getSubDistrict(jobID);
    }

    @Recover
    public JobEntity recover(JobServiceException ex){
        log.info("Job Data not fetched");
        throw new JobServiceException("Data not found for the jobId with error" +ex.getErrorMessage());
    }

    public JobEntity getSubDistrict(String jobId) {
        String uri = jobServicePath.concat("/job/").concat(jobId);
        log.info("Invoking get job details api:{}", uri);
        try {
            ResponseEntity<JobEntity> responseEntity =
                    restTemplate.getForEntity(uri, JobEntity.class);
            log.info("Api invocation successful");
            return responseEntity.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error(
                    "Api request error; ErrorCode:{} ; Message:{}",
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            ErrorResponse error = parseErrorResponse(ex.getResponseBodyAsString());
            throw new JobServiceException(error.getErrorMessage());
        }
    }

    @SneakyThrows
    private ErrorResponse parseErrorResponse(String errorResponse) {
        return objectMapper.readValue(errorResponse, ErrorResponse.class);
    }
}
