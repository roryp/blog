package com.example.demo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmailProcessingFactory {
    private final BlockingQueue<EmailTask> queue;
    private final StatusMonitor monitor;

    public EmailProcessingFactory() {
        queue = new LinkedBlockingQueue<>();
        monitor = new StatusMonitor();
    }

    public BlockingQueue<EmailTask> getQueue() {
        return queue;
    }

    public StatusMonitor getMonitor() {
        return monitor;
    }

    public EmailProducer createProducer(int producerId) {
        return new EmailProducer(queue, producerId, monitor);
    }

    public EmailConsumer createConsumer(int consumerId) {
        return new EmailConsumer(queue, consumerId, monitor);
    }

    public StatusBarFrame createStatusBarFrame() {
        return new StatusBarFrame(queue, monitor);
    }

    public ExecutorService createExecutorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}