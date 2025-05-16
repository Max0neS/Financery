package com.example.financery.service;

import java.util.concurrent.atomic.AtomicLong;

public interface VisitCounterService {

    public void increment();

    public Long getCounter();
}
