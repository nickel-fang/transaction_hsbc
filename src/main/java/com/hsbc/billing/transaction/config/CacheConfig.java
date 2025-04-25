package com.hsbc.billing.transaction.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * @author Nickel Fang 2025/4/24
 */
@Configuration
public class CacheConfig {

    @Bean(name = "cache_transactions")
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("transactions");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .initialCapacity(1000)
                .maximumSize(100_000)
                .recordStats());
        return cacheManager;
    }

    @Bean(name = "cache_duplicatedTransactions")
    public CacheManager cacheManagerForDuplicationCheck() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("duplicatedTransactions");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .initialCapacity(1000)
                .maximumSize(100_000)
                .recordStats());
        return cacheManager;
    }
}
