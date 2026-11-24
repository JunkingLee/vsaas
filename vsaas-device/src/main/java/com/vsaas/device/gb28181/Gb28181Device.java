package com.vsaas.device.gb28181;

import lombok.Data;

@Data
public class Gb28181Device {
    private String deviceId;
    private String name;
    private String manufacturer;
    private String model;
    private String firmware;
    private String transport;
    private String ip;
    private Integer port;
    private Integer status;
    private Long tenantId;
    private String registerTime;
    private String keepaliveTime;
}
