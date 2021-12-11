package com.imooc.elasticlock.oversell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class OversellApplication {
    public static void main(String[] args) {
        SpringApplication.run(OversellApplication.class, args);
    }
}
