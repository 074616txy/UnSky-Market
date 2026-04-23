package com.Market;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
@MapperScan("com.Market.*.mapper")
public class UnSkyApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnSkyApplication.class, args);
        log.info("UnSky Market server started: The Little Vessel Sets Sail");
    }
}

