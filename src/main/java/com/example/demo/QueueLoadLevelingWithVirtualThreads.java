package com.example.demo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

// Represents a task to be processed
record Task(int id, String description) {}

// Producer that generates tasks and adds them to the queue
class Producer implements Runnable {
    private static final AtomicInteger taskIdCounter = new AtomicInteger(0);
    private final BlockingQueue<Task> queue;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final int producerId;

    public Producer(BlockingQueue<Task> queue, int producerId) {
        this.queue = queue;
        this.producerId = producerId;
    }

    @Override
    public void run() {
        try {
            while (running.get()) {
                int taskId = taskIdCounter.incrementAndGet();
                Task task = new Task(taskId, "Task " + taskId + " from Producer " + producerId);
                queue.put(task); // Add task to the queue
                System.out.println("Produced: " + task.description());
                // Simulate variable time taken to produce a task
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

// Consumer that processes tasks from the queue
class Consumer implements Runnable {
    private final BlockingQueue<Task> queue;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final int consumerId;

    public Consumer(BlockingQueue<Task> queue, int consumerId) {
        this.queue = queue;
        this.consumerId = consumerId;
    }

    @Override
    public void run() {
        try {
            while (running.get() || !queue.isEmpty()) {
                Task task = queue.poll(100, TimeUnit.MILLISECONDS); // Retrieve and remove task from the queue
                if (task != null) {
                    System.out.println("Consumer " + consumerId + " processing: " + task.description());
                    // Simulate time taken to process a task
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
        BlockingQueue<Task> queue = new LinkedBlockingQueue<>();

        // Create a custom ThreadFactory for virtual threads
        ThreadFactory virtualThreadFactory = Thread.ofVirtual().factory();

        // Create and start multiple producers using virtual threads
        Producer producer1 = new Producer(queue, 1);
        Producer producer2 = new Producer(queue, 2);
        Thread producerThread1 = virtualThreadFactory.newThread(producer1);
        Thread producerThread2 = virtualThreadFactory.newThread(producer2);
        producerThread1.start();
        producerThread2.start();

        // Create and start multiple consumers using virtual threads
        Consumer consumer1 = new Consumer(queue, 1);
        Consumer consumer2 = new Consumer(queue, 2);
        Thread consumerThread1 = virtualThreadFactory.newThread(consumer1);
        Thread consumerThread2 = virtualThreadFactory.newThread(consumer2);
        consumerThread1.start();
        consumerThread2.start();

        // Let the system run for a specified duration
        Thread.sleep(10000); // Run for 10 seconds

        // Initiate shutdown
        System.out.println("Initiating shutdown...");
        producer1.stop();
        producer2.stop();
        producerThread1.join();
        producerThread2.join();

        // Allow consumers to finish processing remaining tasks
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
