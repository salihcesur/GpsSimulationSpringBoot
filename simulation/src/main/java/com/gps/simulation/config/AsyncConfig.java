package com.gps.simulation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // Minimum thread sayısı
        executor.setMaxPoolSize(50); // Maksimum thread sayısı
        executor.setQueueCapacity(200); // Bekleme kuyruğu kapasitesi
        executor.setThreadNamePrefix("Async-Thread-");
        executor.initialize();
        return executor;
    }
}

