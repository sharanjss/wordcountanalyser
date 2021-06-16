package com.code.challenge.wcanlayser.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class WordCountConfiguration {

    @Value("${max.thread.count}")
    int maxThreadCount;

    @Bean
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreadCount);
    }
}
