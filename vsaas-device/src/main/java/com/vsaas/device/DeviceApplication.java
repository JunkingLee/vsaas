package com.vsaas.device;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.vsaas")
@MapperScan("com.vsaas.device.mapper")
@EnableScheduling
public class DeviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeviceApplication.class, args);
    }
}
