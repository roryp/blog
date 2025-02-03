package com.example.demo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class EmailProducer implements Runnable {

    private static final AtomicInteger emailIdCounter = new AtomicInteger(0);
    private final BlockingQueue<EmailTask> queue;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final int producerId;
    private final StatusMonitor monitor;

    public EmailProducer(BlockingQueue<EmailTask> queue, int producerId, StatusMonitor monitor) {
        this.queue = queue;
        this.producerId = producerId;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Producer-" + producerId);
        try {
            while (running.get()) {
                int emailId = emailIdCounter.incrementAndGet();
                EmailTask task = new EmailTask(emailId, "Email content " + emailId);
                queue.put(task);
                monitor.producedCount.incrementAndGet();
                Thread.sleep(100 + ThreadLocalRandom.current().nextInt(100));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println("Producer " + producerId + " stopped.");
        }
    }

    public void stop() {
        running.set(false);
    }
}