# Queue-Based Load Leveling in Java with Azure Service Bus

## Overview

In today’s dynamic software landscape, managing fluctuating workloads is essential for maintaining both performance and reliability. The **Queue-Based Load Leveling pattern** decouples task production from consumption by introducing an intermediary queue. This design smooths out demand spikes, ensuring that producers and consumers can operate independently without overwhelming the system.

While simple in-memory queues can be effective for smaller or single-node applications, they often fall short in distributed environments. Integrating **Azure Service Bus** introduces a scalable, durable messaging solution that is well-suited for enterprise-level deployments.

## Quick Start: Virtual Threads with Queue-Based Load Leveling

The following example demonstrates the use of virtual threads to process email tasks stored in a `BlockingQueue`. For the complete source code, see [QueueLoadLevelingWithVirtualThreads.java](src/main/java/com/example/demo/QueueLoadLevelingWithVirtualThreads.java).

```java
// Represents an email to be processed, with an ID and content.
record EmailTask(int id, String content) {}

// A producer class that simulates email creation and enqueues the task.
class EmailProducer implements Runnable {
    // Implementation details...
}
```

In this example, a `BlockingQueue` manages the flow of tasks between multiple producers and consumers. Each producer creates an email task with a unique identifier and adds it to the queue. Consumers then retrieve and process these tasks. This in-memory messaging solution works well for smaller-scale applications or single-node setups.

## Why Choose Azure Service Bus?

The [Azure Modern Web App pattern for Java](https://github.com/azure/modern-web-app-pattern-java) demonstrates how Azure Service Bus can enhance messaging in distributed systems. Key benefits include:

- **Auto-Scaling:** Dynamically adjusts to varying workloads.
- **Fault Tolerance:** Ensures resilience in the face of failures.
- **Flexible Provisioning:** Offers robust management of topics and queues for complex scenarios.

These features make Azure Service Bus an ideal choice for applications that require a scalable and resilient messaging infrastructure.

## Implementing Queue-Based Load Leveling with Azure Service Bus

Integrating Azure Service Bus into your Java application enables seamless scaling, reliable message persistence, and improved decoupling between system components. Below is an example of how to implement a producer within the **Modern Web App (MWA) pattern**, where email delivery functionality is refactored from a monolithic Java application into a standalone service.

### Producer Implementation (Main Application)

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

By leveraging Azure Service Bus within the **Queue-Based Load Leveling pattern**, Java applications can build a more robust and scalable architecture. This approach decouples producers from consumers, smooths out workload spikes, and enhances overall system resilience.

For a comprehensive guide and reference implementation, explore the **Modern Web App pattern for Java** on GitHub:  
[Modern Web App pattern for Java](https://github.com/Azure/modern-web-app-pattern-java)

Additionally, consult the [official Azure Service Bus Java documentation](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues) for further details and best practices.