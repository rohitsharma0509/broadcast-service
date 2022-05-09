package com.scb.rider.broadcast.repository.redis;

import com.scb.rider.broadcast.entity.redis.RiderJobEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiderJobRepository extends CrudRepository<RiderJobEntity, String> {
    RiderJobEntity findByRiderId(String riderId);

    RiderJobEntity save(RiderJobEntity riderJobEntity);

}
