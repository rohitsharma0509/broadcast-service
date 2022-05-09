package com.scb.rider.broadcast.repository;

import com.scb.rider.broadcast.entity.BroadcastEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import com.scb.rider.broadcast.model.JobCountAggregationResult;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BroadcastRepository extends MongoRepository<BroadcastEntity, String> {

  Optional<BroadcastEntity> findByJobId(String jobId);

  @Aggregation(pipeline = {"{$match: {broadcastStatus: ?0,expiryTimeForBroadcasting: {$gt: ?2},'riderEntities.riderId': ?1, 'riderEntities.broadcastStatus': 'BROADCASTING'}}","{$count: 'jobCount'}","{$project: { _id: 1, jobCount: 1 } }"})
  AggregationResults<JobCountAggregationResult> findJobCountForRider(String status, String riderId, LocalDateTime expiryTime);
}
