package com.scb.rider.broadcast.constants;

public class BroadcastServiceConstants {

  private BroadcastServiceConstants(){throw new IllegalStateException("Utility class");}

  public static final String ACTIVE_STATUS = "Active";

  public static final String INACTIVE_STATUS = "Inactive";

  public static final String JOB_IN_PROGRESS = "JobInProgress";

  public static final String TYPE = "JOB";

  public static final String ANDROID = "GCM";

  public static final String IOS = "APNS";

  public static final String PRIORITY = "high";

  public static final String TITLE = "Rider ";

  public static final String BODY = "New task is available for you. Please accept it within %s seconds ";

  public static final String SOUND= "default";

  public static final String CLICK_ACTION= "MainActivity";

  public static final String JOB_ACCEPTED_STATUS = "JOB_ACCEPTED";

  public static final String ORDER_CANCELLED_BY_OPERATOR = "ORDER_CANCELLED_BY_OPERATOR";

  public static final String BROADCAST_STATUS = "broadcastStatus";
  public static final String BROADCAST_JOBID_KEY   = "jobId";

  public static final String LAST_BROADCAST_DATE_TIME = "lastBroadcastDateTime";

  public static final String EXPIRY_TIME_FOR_BROADCASTING = "expiryTimeForBroadcasting";

  public static final String RIDER_NOT_FOUND_STATUS = "RIDER_NOT_FOUND";

  public static final String COMPLETED_STATUS = "COMPLETED";

  public static final Long DEFAULT_REDIS_CACHE_TTL = 300L;

  public static final String RIDER_JOB_CACHE_KEY_PREFIX = "RiderJobEntity_";

  public static final String JOB_FLASH_DELAY = "jobFlashDelay";
}
