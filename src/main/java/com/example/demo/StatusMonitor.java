package com.example.demo;

import java.util.concurrent.atomic.AtomicInteger;

public class StatusMonitor {
    public final AtomicInteger producedCount = new AtomicInteger(0);
    public final AtomicInteger processedCount = new AtomicInteger(0);
    public final AtomicInteger activeProcessingCount = new AtomicInteger(0);
}