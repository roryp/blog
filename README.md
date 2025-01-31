# Queue-Based Load Leveling in Java with Azure Service Bus

## Introduction

In today's software landscape, handling fluctuating workloads efficiently is crucial for performance and reliability. The **Queue-Based Load Leveling pattern** introduces a queue between producers and consumers, ensuring tasks are decoupled from processing. This approach helps manage spikes in demand without overwhelming the system.

While in-memory queues offer a straightforward solution, they have limitations in distributed environments. Integrating **Azure Service Bus** provides a scalable, durable messaging option. 

## Simple Introduction: Virtual Threads with Queue-Based Load Leveling

Below is a quick example that uses virtual threads to process tasks in a `BlockingQueue`. See [QueueLoadLevelingWithVirtualThreads.java](src/main/java/com/example/demo/QueueLoadLevelingWithVirtualThreads.java):

```java
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
                queue.put(task);
                System.out.println("Produced: " + task.description());
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
                Task task = queue.poll(100, TimeUnit.MILLISECONDS);
                if (task != null) {
                    System.out.println("Consumer " + consumerId + " processing: " + task.description());
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
        ThreadFactory virtualThreadFactory = Thread.ofVirtual().factory();

        Producer producer1 = new Producer(queue, 1);
        Producer producer2 = new Producer(queue, 2);
        Thread producerThread1 = virtualThreadFactory.newThread(producer1);
        Thread producerThread2 = virtualThreadFactory.newThread(producer2);
        producerThread1.start();
        producerThread2.start();

        Consumer consumer1 = new Consumer(queue, 1);
        Consumer consumer2 = new Consumer(queue, 2);
        Thread consumerThread1 = virtualThreadFactory.newThread(consumer1);
        Thread consumerThread2 = virtualThreadFactory.newThread(consumer2);
        consumerThread1.start();
        consumerThread2.start();

        Thread.sleep(10000);
        System.out.println("Initiating shutdown...");
        producer1.stop();
        producer2.stop();
        producerThread1.join();
        producerThread2.join();

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
```

This example is available in this repository** as an introductory demonstration of Queue-Based Load Leveling. It illustrates the principles of load balancing but lacks durability and scalability for distributed environments.

## Implementing Queue-Based Load Leveling with Azure Service Bus

Azure Service Bus enables seamless scaling, message persistence, and better decoupling. Below is an implementation using Azure Service Bus within the **Modern Web App (MWA) pattern**, where email delivery functionality is extracted from a monolithic Java application into a standalone service.

### Producer (Main Application)

```java
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class SupportGuideQueueSender {

    private final StreamBridge streamBridge;

    public SupportGuideQueueSender(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void send(String to, String guideUrl, Long requestId) {
        EmailRequest emailRequest = EmailRequest.newBuilder()
                .setRequestId(requestId)
                .setEmailAddress(to)
                .setUrlToManual(guideUrl)
                .build();

        streamBridge.send("emailRequest-out-0", emailRequest);
    }
}
```

## Conclusion

By leveraging Azure Service Bus within the **Queue-Based Load Leveling pattern**, Java applications can achieve a more robust and scalable architecture, effectively handling varying workloads and enhancing overall system resilience.

For a comprehensive guide and reference implementation, explore the **Modern Web App pattern for Java**: [https://github.com/Azure/modern-web-app-pattern-java](https://github.com/Azure/modern-web-app-pattern-java)

Additionally, refer to the official Azure Service Bus Java documentation: [https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues)
