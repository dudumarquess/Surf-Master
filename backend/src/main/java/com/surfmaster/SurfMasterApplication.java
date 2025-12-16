package com.surfmaster;

import java.time.Clock;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import com.surfmaster.config.ForecastProperties;

@SpringBootApplication
@EnableConfigurationProperties(ForecastProperties.class)
public class SurfMasterApplication {
    public static void main(String[] args) {
        SpringApplication.run(SurfMasterApplication.class, args);

    }

    @Bean
    @Transactional
    public CommandLineRunner demo() {
        return (args) -> {
            System.out.println("do some sanity tests here");
        };
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
