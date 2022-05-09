package com.scb.rider.broadcast.scheduler;

import static com.scb.rider.broadcast.constants.RiderBroadcastStatus.BROADCASTING;
import static com.scb.rider.broadcast.constants.RiderBroadcastStatus.PENDING;

import com.scb.rider.broadcast.constants.RiderBroadcastStatus;
import com.scb.rider.broadcast.entity.BroadcastEntity;
import com.scb.rider.broadcast.entity.Details;
import com.scb.rider.broadcast.entity.RiderEntity;
import com.scb.rider.broadcast.entity.redis.RiderJobEntity;
import com.scb.rider.broadcast.model.request.JobAllocationRequest;
import com.scb.rider.broadcast.model.request.Location;
import com.scb.rider.broadcast.model.request.Rider;
import com.scb.rider.broadcast.repository.BroadcastRepository;
import com.scb.rider.broadcast.repository.redis.RiderJobRepository;
import com.scb.rider.broadcast.service.BroadcastService;
import com.scb.rider.broadcast.service.RiderJobCacheService;
import com.scb.rider.broadcast.service.proxy.JobAllocationProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class JobReBroadcastTask implements Runnable {

  List<BroadcastEntity> jobsList;
  BroadcastRepository broadcastRepository;
  BroadcastService broadcastService;
  JobAllocationProxy jobAllocationProxy;
  RiderJobCacheService riderJobCacheService;


  public JobReBroadcastTask(
      List<BroadcastEntity> jobsList,
      BroadcastRepository broadcastRepository,
      BroadcastService broadcastService,
      JobAllocationProxy jobAllocationProxy,
      RiderJobCacheService riderJobCacheService){

    this.jobsList = jobsList;
    this.broadcastRepository=broadcastRepository;
    this.broadcastService=broadcastService;
    this.jobAllocationProxy=jobAllocationProxy;
    this.riderJobCacheService = riderJobCacheService;
  }

  @Override
  public void run() {

    log.info("re-broadcasting th jobs to next set of riders");
    jobsList.forEach(broadcastEntity->{

      int batchCount = broadcastEntity.getBatchCount();
      List<String> ridersNotAcceptedList = new ArrayList<>(broadcastEntity.getRiderEntities().size());

      for (RiderEntity entity : broadcastEntity.getRiderEntities()) {
        entity.setBroadcastStatus(RiderBroadcastStatus.NOT_ACCEPTED.name());
        ridersNotAcceptedList.add(entity.getRiderId());
      }

      broadcastRepository.save(broadcastEntity);

      riderJobCacheService.deleteJobForRiders(ridersNotAcceptedList, broadcastEntity.getJobId());

      // for each job , call the job allocation service to get the next set of riders
      Rider[] nextSetOfRiders =
          jobAllocationProxy.getNextSetOfRiders(
              getJobAllocationRequest(broadcastEntity), (batchCount+1) * broadcastEntity.getMaxRidersForJob());

      List<RiderEntity> riderEntityList = broadcastEntity.getRiderEntities();
      List<RiderEntity> oldRiderEntityList = riderEntityList;
      int newRidersCount = 0;

      if (nextSetOfRiders != null) {
        // get count for new riders
        for (Rider rider : nextSetOfRiders) {

          if (!searchEntity(rider, riderEntityList)) {

            newRidersCount += 1;
            RiderEntity riderEntity =
                RiderEntity.builder()
                    .riderId(rider.getRiderId())
                    .broadcastStatus(PENDING.name())
                    .distance(rider.getDistance())
                    .build();
            riderEntityList.add(riderEntity);
          }

        }

        broadcastEntity.setRiderEntities(riderEntityList);

        if (newRidersCount != broadcastEntity.getMaxRidersForJob() ) {

          for (RiderEntity entity : oldRiderEntityList) {
            if (entity.getBatchCount()==batchCount) {
              entity.setBroadcastStatus(PENDING.name());
            }
          }
        }
        // call the broadcast service to broadcast the job to the riders
        broadcastService.broadcastForRider(broadcastEntity,batchCount+1, true);

      } else {
        log.info("No riders found for re-broadcasting for job " + broadcastEntity.getJobId());
      }

    });
  }

  private boolean searchEntity(Rider rider, List<RiderEntity> riderEntitiesList) {
    return riderEntitiesList.stream()
        .anyMatch(
            riderEntity -> {
              if (riderEntity.getRiderId().equals(rider.getRiderId())) {
                return true;
              }
              return false;
            });
  }

  private JobAllocationRequest getJobAllocationRequest(BroadcastEntity broadcastEntity) {

    JobAllocationRequest jobAllocationRequest = new JobAllocationRequest();
    jobAllocationRequest.setJobId(broadcastEntity.getJobId());
    jobAllocationRequest.setJobType(String.valueOf(broadcastEntity.getJobType().ordinal()+1));

    Details merchantDetails = broadcastEntity.getMerchantDetails();
    Details customerDetails = broadcastEntity.getCustomerDetails();

    Location[] locationList = new Location[2];
    for (int i = 0; i <= 1; i++) {
      Location location = new Location();
      if (i == 0) {
        location.setAddress(merchantDetails.getAddress());
        location.setContactPhone(merchantDetails.getPhone());
        location.setContactName(merchantDetails.getName());
        location.setLat(merchantDetails.getLocation().getLatitude());
        location.setLng(merchantDetails.getLocation().getLongitude());
        location.setSeq(1);
      }
      if (i == 1) {
        location.setAddress(customerDetails.getAddress());
        location.setContactPhone(customerDetails.getPhone());
        location.setContactName(customerDetails.getName());
        location.setLat(customerDetails.getLocation().getLatitude());
        location.setLng(customerDetails.getLocation().getLongitude());
        location.setSeq(2);
      }
      locationList[i] = location;
    }
    jobAllocationRequest.setLocationList(locationList);
    return jobAllocationRequest;
  }
}
