package com.pmo.userservice.domain.multitenancy.async;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig extends AsyncConfigurerSupport {

    @Value("${multi-tenancy.async-executor.core-pool-size}")
    private int corePoolSize;

    @Value("${multi-tenancy.async-executor.max-pool-size}")
    private int maxPoolSize;

    @Value("${multi-tenancy.async-executor.queue-capacity}")
    private int queueCapacity;

    @Value("${multi-tenancy.async-executor.thread-name-prefix}")
    private String threadNamePrefix;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setTaskDecorator(new TenantAwareTaskDecorator());
        executor.initialize();

        return executor;
    }

}
