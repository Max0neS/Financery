package com.example.financery.service.impl;

import com.example.financery.service.AsyncLogExecutor;
import com.example.financery.service.LogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncLogExecutorImpl implements AsyncLogExecutor {

    private final LogService logService;

    public AsyncLogExecutorImpl(LogService logService) {
        this.logService = logService;
    }

    @Async("executor")
    @Override
    public void executeCreateLogs(Long taskId, String date) {
        logService.createLogs(taskId, date);
    }
}