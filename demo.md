# Deep Dive into the Email Processor App

## Overview

This document provides a detailed look into the architecture, components, and workflow of the email processor app. The app is implemented in Java and uses Azure Service Bus for queue-based load leveling.

## Architecture

The email processor app follows a queue-based load leveling pattern to handle dynamic workloads efficiently. The main components of the app are:

- **EmailProducer**: Generates email tasks and enqueues them.
- **EmailConsumer**: Processes email tasks asynchronously.
- **StatusMonitor**: Tracks the status of email processing.
- **LangChainLLMReportGenerator**: Generates a performance report using LangChain4j.

## Components

### EmailProducer

The `EmailProducer` class is responsible for generating email tasks and adding them to the queue. Here is a code snippet:

```java
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
```

### EmailConsumer

The `EmailConsumer` class processes email tasks asynchronously. Here is a code snippet:

```java
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
```

### LangChainLLMReportGenerator

The `LangChainLLMReportGenerator` class generates a performance report using LangChain4j. Here is a code snippet:

```java
public class LangChainLLMReportGenerator {
    public void generateReport(StatusMonitor monitor) {
        String prompt = String.format(
            "Generate a final report on email processing metrics. Emails Produced: %d, Emails Processed: %d, Active Processing: %d. " +
            "Explain whether all emails were processed successfully or if there is a discrepancy, and summarize overall performance.",
            monitor.producedCount.get(), 
            monitor.processedCount.get(), 
            monitor.activeProcessingCount.get()
        );

        OllamaStreamingLanguageModel model = OllamaStreamingLanguageModel.builder()
            .baseUrl("http://localhost:11434")
            .modelName("phi4")
            .temperature(0.7)
            .build();

        model.generate(prompt, new StreamingResponseHandler<String>() {
            @Override
            public void onNext(String token) {
                System.out.print(token);
            }
        
            @Override
            public void onComplete(Response<String> response) {
                System.out.println("onComplete: " + response);
            }
        
            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
            }
        });
    }
}
```

## Workflow

1. **EmailProducer** generates email tasks and enqueues them.
2. **EmailConsumer** processes email tasks asynchronously.
3. **StatusMonitor** tracks the status of email processing.
4. **LangChainLLMReportGenerator** generates a performance report.

## Role of Azure Service Bus

Azure Service Bus plays a crucial role in the queue-based load leveling pattern. It decouples the production and consumption of tasks, ensuring that the system remains responsive even during traffic spikes. Here are some key benefits:

- **Decoupling**: Offloads message processing to keep the main application responsive.
- **Scalability**: Automatically adjusts to workload changes without overprovisioning.
- **Resilience**: Features like message persistence, automatic retries, and dead-letter queues ensure the system can handle failures gracefully.

By integrating Azure Service Bus, the email processor app achieves enhanced scalability, resilience, and responsiveness.
