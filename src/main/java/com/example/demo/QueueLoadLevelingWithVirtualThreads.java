package com.example.demo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

// Represents an email to be processed
record EmailTask(int id, String content) {}

// Producer-like class that simulates creating emails and adding them to the queue
class EmailProducer implements Runnable {
    private static final AtomicInteger emailIdCounter = new AtomicInteger(0);
    private final BlockingQueue<EmailTask> queue;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final int producerId;

    public EmailProducer(BlockingQueue<EmailTask> queue, int producerId) {
        this.queue = queue;
        this.producerId = producerId;
    }

    @Override
    public void run() {
        try {
            while (running.get()) {
                int emailId = emailIdCounter.incrementAndGet();
                EmailTask email = new EmailTask(
                    emailId, "Email " + emailId + " from Producer " + producerId
                );
                queue.put(email);
                System.out.println("Produced email: " + email.content());
                // Simulate time to produce an email
                Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
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

// Consumer-like class that simulates sending emails
class EmailConsumer implements Runnable {
    private final BlockingQueue<EmailTask> queue;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final int consumerId;

    public EmailConsumer(BlockingQueue<EmailTask> queue, int consumerId) {
        this.queue = queue;
        this.consumerId = consumerId;
    }

    @Override
    public void run() {
        try {
            while (running.get() || !queue.isEmpty()) {
                EmailTask email = queue.poll(100, TimeUnit.MILLISECONDS);
                if (email != null) {
                    System.out.println("Consumer " + consumerId + " sending: " + email.content());
                    // Simulate time to send each email
                    Thread.sleep(ThreadLocalRandom.current().nextInt(200, 1000));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println("Consumer " + consumerId + " stopped.");
        }
    }

    public void stop() {
        running.set(false);
    }
}

public class QueueLoadLevelingWithVirtualThreads {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<EmailTask> queue = new LinkedBlockingQueue<>();

        // Use a custom ThreadFactory for virtual threads
        ThreadFactory virtualThreadFactory = Thread.ofVirtual().factory();

        // Create and start multiple EmailProducers
        EmailProducer producer1 = new EmailProducer(queue, 1);
        EmailProducer producer2 = new EmailProducer(queue, 2);
        Thread producerThread1 = virtualThreadFactory.newThread(producer1);
        Thread producerThread2 = virtualThreadFactory.newThread(producer2);
        producerThread1.start();
        producerThread2.start();

        // Create and start multiple EmailConsumers
        EmailConsumer consumer1 = new EmailConsumer(queue, 1);
        EmailConsumer consumer2 = new EmailConsumer(queue, 2);
        Thread consumerThread1 = virtualThreadFactory.newThread(consumer1);
        Thread consumerThread2 = virtualThreadFactory.newThread(consumer2);
        consumerThread1.start();
        consumerThread2.start();

        // Let the system run for a short duration
        Thread.sleep(10000);

        // Initiate shutdown
        System.out.println("Initiating shutdown...");
        producer1.stop();
        producer2.stop();
        producerThread1.join();
        producerThread2.join();

        // Let consumers finish remaining emails
        while (!queue.isEmpty()) {
            Thread.sleep(100);
        }

        consumer1.stop();
        consumer2.stop();
        consumerThread1.join();
        consumerThread2.join();

        System.out.println("Shutdown complete.");
    }
}
