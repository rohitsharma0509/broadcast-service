package com.scb.rider.broadcast.model.response;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class JobLocation {
    private int seq;
    private String type;
    private String addressId;
    private String addressName;
    private String address;
    @NotBlank
    private String lat;
    @NotBlank
    private String lng;
    private String contactName;
    private String contactPhone;
    private String actualArriveTime;
    private String mail;

    private String subDistrict;
}
