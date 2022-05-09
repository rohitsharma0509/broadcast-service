package com.scb.rider.broadcast.util;

import com.scb.rider.broadcast.constants.JobType;
import com.scb.rider.broadcast.entity.BroadcastEntity;
import com.scb.rider.broadcast.entity.Details;
import com.scb.rider.broadcast.entity.LatLongLocation;
import com.scb.rider.broadcast.entity.RiderEntity;
import com.scb.rider.broadcast.model.request.BroadcastRequest;
import com.scb.rider.broadcast.model.request.JobDetails;
import com.scb.rider.broadcast.model.request.Location;
import com.scb.rider.broadcast.model.request.Rider;
import com.scb.rider.broadcast.model.response.RiderDeviceDetailsResponse;
import com.scb.rider.broadcast.model.response.RiderProfileDetails;
import com.scb.rider.broadcast.model.response.RiderProfileResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BroadcastServiceUtils {

  public static RiderProfileResponse getRiderProfileResponse(String riderId, String status) {
    String availabilityStatus = "";
    if (status.equals("available")) availabilityStatus = "Active";
    else availabilityStatus = "Inactive";

    return RiderProfileResponse.builder().riderProfileDetails(RiderProfileDetails.builder()
        .id(riderId)
        .firstName("Jason")
        .lastName("Smith")
        .availabilityStatus(availabilityStatus)
        .build()).riderDeviceDetails(RiderDeviceDetailsResponse.builder().arn("arn").platform("GCM").build())
        .build();
  }


  public static RiderDeviceDetailsResponse getRiderDeviceDetailsResponse(String riderId) {
    return RiderDeviceDetailsResponse.builder().id(riderId).arn("abcd1234").platform("GCM").build();
  }

  public static List<BroadcastEntity> getListOfExpiredJobs()
  {
    List<BroadcastEntity> broadcastEntityList = new ArrayList<BroadcastEntity>();
    BroadcastEntity broadcastEntity1 = BroadcastEntity.builder().jobId("abcd").riderEntities(getRiderEntityWithTwoRiders()).jobType(JobType.MART).customerDetails(Details.builder().location(
        LatLongLocation.builder().build()).build()).merchantDetails(Details.builder().location(
        LatLongLocation.builder().build()).build()).maxJobsForRider(5).maxRidersForJob(5).build();


    BroadcastEntity broadcastEntity2 = BroadcastEntity.builder().jobId("efgh").riderEntities(getRiderEntityWithTwoRiders()).jobType(JobType.MART).customerDetails(Details.builder().location(
        LatLongLocation.builder().build()).build()).merchantDetails(Details.builder().location(
        LatLongLocation.builder().build()).build()).maxJobsForRider(5).maxRidersForJob(5).build();
    broadcastEntityList.add(broadcastEntity1);
    broadcastEntityList.add(broadcastEntity2);

    return broadcastEntityList;


  }

  public static List<BroadcastEntity> getListOfJobs(int noOfRiders)
  {
    List<BroadcastEntity> broadcastEntityList = new ArrayList<BroadcastEntity>();

    if(noOfRiders==1)
    {
      BroadcastEntity broadcastEntity1 = BroadcastEntity.builder()
          .broadcastStatus("WAITING")
          .expiryTimeForBroadcasting(LocalDateTime.now())
          .riderEntities(getRiderEntityWithOneRider())
          .jobId("abcd").build();
      broadcastEntityList.add(broadcastEntity1);
    }
    if(noOfRiders==2)
    {
      BroadcastEntity broadcastEntity1 = BroadcastEntity.builder()
          .riderEntities(getRiderEntityWithTwoRiders())
          .jobId("abcd")
          .expiryTimeForBroadcasting(LocalDateTime.now())
          .merchantDetails(Details.builder().build())
          .customerDetails(Details.builder().build())
          .remark("a")
          .price(1.0)
          .distance(1.0)
          .expiryTimeForBroadcasting(LocalDateTime.now())
          .build();

      BroadcastEntity broadcastEntity2 = BroadcastEntity.builder()
          .riderEntities(getRiderEntityWithTwoRiders())
          .expiryTimeForBroadcasting(LocalDateTime.now())
          .jobId("efgh")
          .merchantDetails(Details.builder().build())
          .customerDetails(Details.builder().build())
          .remark("a")
          .price(1.0)
          .distance(1.0)
          .expiryTimeForBroadcasting(LocalDateTime.now())
          .build();

      broadcastEntityList.add(broadcastEntity1);
      broadcastEntityList.add(broadcastEntity2);
    }


    return broadcastEntityList;


  }

  public static BroadcastRequest getBroadcastRequest(int riderCount) {

    List<Location> locationList = new ArrayList<Location>();
    Location location1 = new Location();
    location1.setAddressName("");
    location1.setAddress("Bellandur Gate, Sarjapura Road, Near Wipro Corp Office, zip - 560035");
    location1.setLat("80.42432");
    location1.setLng("-12.35343");
    location1.setContactName("Domino's Pizza");
    location1.setContactPhone("+918023422342");
    location1.setSeq(1);
    locationList.add(location1);

    Location location2 = new Location();
    location2.setAddressName("");
    location2.setAddress("#B-506, Confident Aquila, Sarjapura Road, Carmelaram, zip - 560037");
    location2.setLat("80.42432");
    location2.setLng("-12.35343");
    location2.setContactName("John Martin");
    location2.setContactPhone("+918023422342");
    location2.setSeq(2);
    locationList.add(location2);

    List<Rider> riderList = new ArrayList<Rider>();
    if (riderCount == 0) riderList = null;
    if (riderCount == 1) riderList = getRidersWithOneRider();
    if (riderCount == 2) riderList = getRidersWithTwoRiders();
    if (riderCount == 3) riderList = getRidersWithThreeRiders();

    return BroadcastRequest.builder()
        .jobDetails(
            JobDetails.builder()
                .jobId("RBH201205123456")
                .netPrice(100)
                .remark("Should be served in 30 minutes")
                .locationList(locationList)
                .build())
        .riders(riderList)
        .maxJobsForRider(5)
        .maxRidersForJob(5)
        .build();
  }

  private static List<RiderEntity> getRiderEntityWithOneRider() {
    List<RiderEntity> riderList = new ArrayList<RiderEntity>();
    RiderEntity rider1 = new RiderEntity();
    rider1.setRiderId("abcd");
    rider1.setBroadcastStatus("BROADCASTING");
    riderList.add(rider1);
    return riderList;
  }

  private static List<RiderEntity> getRiderEntityWithTwoRiders() {
    List<RiderEntity> riderList = new ArrayList<RiderEntity>();
    RiderEntity rider1 = new RiderEntity();
    rider1.setRiderId("abcd");
    rider1.setExpiryTimeForBroadcasting(LocalDateTime.now());
    rider1.setBroadcastStatus("BROADCASTING");
    riderList.add(rider1);
    RiderEntity rider2 = new RiderEntity();
    rider2.setRiderId("efgh");
    rider2.setExpiryTimeForBroadcasting(LocalDateTime.now());
    rider2.setBroadcastStatus("PENDING");
    riderList.add(rider2);
    return riderList;
  }

  private static List<Rider> getRidersWithTwoRiders() {
    List<Rider> riderList = new ArrayList<Rider>();
    Rider rider1 = new Rider();
    rider1.setRiderId("abcd");
    riderList.add(rider1);
    Rider rider2 = new Rider();
    rider2.setRiderId("efgh");
    riderList.add(rider2);

    return riderList;
  }

  private static List<Rider> getRidersWithOneRider() {
    List<Rider> riderList = new ArrayList<Rider>();
    Rider rider1 = new Rider();
    rider1.setRiderId("abcd");
    rider1.setDistance(10.00);
    riderList.add(rider1);
    return riderList;
  }

  private static List<Rider> getRidersWithThreeRiders() {
    List<Rider> riderList = new ArrayList<Rider>();
    Rider rider1 = new Rider();
    rider1.setRiderId("abcd");
    riderList.add(rider1);
    Rider rider2 = new Rider();
    rider2.setRiderId("efgh");
    riderList.add(rider2);
    Rider rider3 = new Rider();
    rider2.setRiderId("ijkl");
    riderList.add(rider3);

    return riderList;
  }

  public static Rider[] getRiders()
  {
    Rider rider1 = new Rider();
    rider1.setRiderId("abcd1");

    Rider[] riders = new Rider [1];
    for(int i=0;i<riders.length;i++)
    {
      riders[i]=rider1;
    }

    return riders;

  }

}
