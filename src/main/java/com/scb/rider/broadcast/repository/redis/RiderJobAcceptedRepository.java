package com.scb.rider.broadcast.repository.redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.scb.rider.broadcast.entity.redis.RiderJobAcceptedEntity;

@Repository
public interface RiderJobAcceptedRepository extends CrudRepository<RiderJobAcceptedEntity, String> {
    
}
