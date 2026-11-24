package com.vsaas.record;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.vsaas")
@EnableScheduling
@MapperScan("com.vsaas.record.mapper")
public class RecordApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecordApplication.class, args);
    }
}
