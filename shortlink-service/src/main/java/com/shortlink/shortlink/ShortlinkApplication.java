package com.shortlink.shortlink;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.shortlink")
@MapperScan("com.shortlink.shortlink.mapper")
public class ShortlinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortlinkApplication.class, args);
    }
}
