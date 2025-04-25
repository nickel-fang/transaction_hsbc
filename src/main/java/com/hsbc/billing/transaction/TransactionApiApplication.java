package com.hsbc.billing.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author Nickel Fang 2025/4/24
 */
@SpringBootApplication
@EnableCaching
public class TransactionApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionApiApplication.class, args);
    }
}
