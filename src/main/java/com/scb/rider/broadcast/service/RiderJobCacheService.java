package com.scb.rider.broadcast.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.scb.rider.broadcast.constants.BroadcastServiceConstants;
import com.scb.rider.broadcast.model.kafka.RiderBroadcastRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.scb.rider.broadcast.entity.BroadcastEntity;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RiderJobCacheService {

    @Autowired
    private RedisCacheService redisCacheService;

    public void saveOrUpdateRiderCache(String riderId, RiderBroadcastRequest riderBroadcastRequest) {
        log.info("updating cache for key {}, hashKey {}", riderId, riderBroadcastRequest.getJobId());
        redisCacheService.put(getCacheKey(riderId), riderBroadcastRequest.getJobId(), riderBroadcastRequest);
    }

    public List<RiderBroadcastRequest> getAllJobsForRider(String riderId) {
        log.info("getting all the jobs from cache for riderId {}", riderId);
        List<Object> cachedValues = redisCacheService.values(getCacheKey(riderId));
        if(CollectionUtils.isEmpty(cachedValues)) {
            return Collections.emptyList();
        }
        return cachedValues.stream().map(obj -> (RiderBroadcastRequest)obj).collect(Collectors.toList());
    }

    public Long getNumberOfJobsForRider(String riderId) {
        log.info("getting number of jobs from cache for riderId {}", riderId);
        return redisCacheService.size(getCacheKey(riderId));
    }

    public void deleteAllJobsForRider(String riderId) {
        log.info("deleting all the jobs from cache for riderId {}", riderId);
        redisCacheService.deleteAll(getCacheKey(riderId));
    }

    public void deleteJobForRidersByBroadcastEntity(BroadcastEntity broadcastEntity) {
        if(Objects.nonNull(broadcastEntity) && !CollectionUtils.isEmpty(broadcastEntity.getRiderEntities())) {
            broadcastEntity.getRiderEntities().stream().forEach(riderEntity ->
                deleteJobForRider(riderEntity.getRiderId(), broadcastEntity.getJobId())
            );
        }
    }

    public void deleteJobForRiders(List<String> riderIds, String jobId) {
        if(!CollectionUtils.isEmpty(riderIds)) {
            riderIds.stream().forEach(riderId -> deleteJobForRider(riderId, jobId));
        }
    }

    public void deleteJobForRider(String riderId, String jobId) {
        log.info("deleting cache for riderId {}, jobId {}", riderId, jobId);
        redisCacheService.delete(getCacheKey(riderId), jobId);
    }

    private String getCacheKey(String riderId) {
        StringBuilder sb = new StringBuilder(BroadcastServiceConstants.RIDER_JOB_CACHE_KEY_PREFIX);
        sb.append(riderId);
        return sb.toString();
    }
}
