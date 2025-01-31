# Queue-Based Load Leveling in Java with Azure Service Bus

## Introduction

In today's software landscape, handling fluctuating workloads efficiently is crucial for performance and reliability. The **Queue-Based Load Leveling pattern** introduces a queue between producers and consumers, ensuring tasks are decoupled from processing. This approach helps manage spikes in demand without overwhelming the system.

While in-memory queues offer a straightforward solution, they have limitations in distributed environments. Integrating **Azure Service Bus** provides a scalable, durable messaging option. 

## Simple Introduction: Virtual Threads with Queue-Based Load Leveling

Below is a quick example that uses virtual threads to process tasks in a `BlockingQueue`. See [QueueLoadLevelingWithVirtualThreads.java](src/main/java/com/example/demo/QueueLoadLevelingWithVirtualThreads.java):

```java
// Represents a task to be processed, storing an ID and description
record Task(int id, String description) {}

// Producer that generates tasks and adds them to the queue
class Producer implements Runnable {
    // Atomic counter to assign unique IDs to each task
    private static final AtomicInteger taskIdCounter = new AtomicInteger(0);
    // BlockingQueue shared between producers and consumers
    private final BlockingQueue<Task> queue;
    // Used to signal when production should stop
    private final AtomicBoolean running = new AtomicBoolean(true);
    // Identifies which producer is running
    private final int producerId;

    public Producer(BlockingQueue<Task> queue, int producerId) {
        this.queue = queue;
        this.producerId = producerId;
    }
}
```

This example uses a `BlockingQueue` to manage tasks produced by multiple producers and consumed by multiple consumers. Each `Producer` creates a `Task` with a unique ID and places it into the queue, while the `Consumer` reads tasks and processes them. This approach demonstrates a straightforward in-memory messaging solution that works well for smaller applications or single-node setups.

## Comparison with Azure Service Bus

In contrast, the [Azure Modern Web App pattern for Java](https://github.com/azure/modern-web-app-pattern-java) shows how tasks are communicated through Azure Service Bus. This allows for distributed, scalable messaging across multiple nodes or microservices. Service Bus includes built-in features like auto-scaling, fault tolerance, and flexible provisioning of topics and queues, making it suitable for enterprise-level deployments.

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
