package com.skripty.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Thread pool used for AI generation calls (see DocumentService#generateAsync).
 * This is what lets multiple users trigger "Generate" at the same time without
 * blocking each other or the web server's own request-handling threads: each
 * generation job runs on a worker from this pool while the HTTP thread that
 * accepted the upload is freed immediately to serve the next request.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "geminiTaskExecutor")
    public Executor geminiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("gemini-gen-");
        executor.initialize();
        return executor;
    }
}
