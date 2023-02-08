package com.capstone.year.TaskExecutors;

import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class TaskExecutor {

    // Match available system resources
    int corePoolSize = Runtime.getRuntime().availableProcessors();

    // Multithreading taskExecutor (using SimpleAsyncTaskExecutor)
    @Bean("taskExecutor_Year")
    public org.springframework.core.task.TaskExecutor asyncTaskExecutor() {

        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(corePoolSize);
        return simpleAsyncTaskExecutor;
    }
}
