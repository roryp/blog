package com.example.demo;

import java.util.concurrent.BlockingQueue;

public class EmailConsumer implements Runnable {

    private final BlockingQueue<EmailTask> queue;
    private final int consumerId;
    private final StatusMonitor monitor;

    public EmailConsumer(BlockingQueue<EmailTask> queue, int consumerId, StatusMonitor monitor) {
        this.queue = queue;
        this.consumerId = consumerId;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Consumer-" + consumerId);
        try {
            while (true) {
                EmailTask task = queue.take();
                monitor.activeProcessingCount.incrementAndGet();
                processTask(task);
                monitor.processedCount.incrementAndGet();
                monitor.activeProcessingCount.decrementAndGet();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println("Consumer " + consumerId + " stopped.");
        }
    }

    private void processTask(EmailTask task) throws InterruptedException {
        // Simulate processing delay.
        Thread.sleep(200);
    }
}