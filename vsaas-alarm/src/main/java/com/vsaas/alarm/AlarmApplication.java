package com.vsaas.alarm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.vsaas")
@MapperScan("com.vsaas.alarm.mapper")
public class AlarmApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlarmApplication.class, args);
    }
}
