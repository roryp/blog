# Queue-Based Load Leveling in Java with Azure Service Bus

## Overview

In today’s dynamic software landscape, managing fluctuating workloads is essential for maintaining both performance and reliability. The **Queue-Based Load Leveling pattern** decouples task production from consumption by introducing an intermediary queue. This design smooths out demand spikes, ensuring that producers and consumers can operate independently without overwhelming the system.

While in-memory queues work well for small-scale or single-node applications, they often fall short in distributed or enterprise environments. By integrating **Azure Service Bus**, you can leverage a scalable, durable messaging solution that meets the demands of modern cloud applications.

## Features

- **Decoupling:** Separates the production of tasks from their consumption, ensuring smoother performance under variable loads.
- **Scalability:** Handles workload spikes by offloading messages to a queue, allowing producers and consumers to scale independently.
- **Fault Tolerance:** Provides resilience against failures with features such as message persistence and dead-letter queues (when using Azure Service Bus).
- **Modern Concurrency:** Utilizes Java virtual threads for lightweight concurrency, preparing your application for high throughput.

## Prerequisites

- **Java 17+** – Required for virtual threads and modern language features.
- **Maven/Gradle** – For building and running the project.
- (Optional) **Azure Subscription** – For testing Azure Service Bus integration.
- Familiarity with basic Java concurrency and messaging patterns.

## Quick Start: Virtual Threads with In-Memory Queue

The following example demonstrates a simple simulation of the Queue-Based Load Leveling pattern using Java’s virtual threads and an in-memory `BlockingQueue`. This sample shows how multiple producers and consumers coordinate via a shared queue.

For the complete source code, see: [QueueLoadLevelingWithVirtualThreads.java](src/main/java/com/example/demo/QueueLoadLevelingWithVirtualThreads.java).

```java
// Represents an email to be processed, with an ID and content.
record EmailTask(int id, String content) {}

// A producer class that simulates email creation and enqueues the task.
class EmailProducer implements Runnable {
    // Implementation details...
}
```

**How It Works:**

- **Producers:** Generate email tasks (each with a unique ID) and place them in a `BlockingQueue`.
- **Consumers:** Retrieve and process these tasks asynchronously.
- **Virtual Threads:** Manage concurrent tasks efficiently with minimal overhead, paving the way for scalable solutions.

This in-memory solution is ideal for demonstration purposes and smaller applications.

## Comparison with Azure Service Bus

While the in-memory queue approach is excellent for local testing and simple use cases, enterprise-level applications benefit greatly from using a fully managed messaging service such as **Azure Service Bus**. The [Azure Modern Web App pattern for Java](https://github.com/azure/modern-web-app-pattern-java) showcases how Service Bus enhances distributed messaging with these key benefits:

- **Auto-Scaling:** Automatically adjusts to variable workloads.
- **Fault Tolerance:** Provides built-in features to handle transient failures.
- **Flexible Provisioning:** Offers advanced management of topics and queues, supporting complex messaging scenarios.

These advantages make Azure Service Bus an ideal choice when you need to decouple application components across distributed environments.

## Implementing Queue-Based Load Leveling with Azure Service Bus

Integrating Azure Service Bus into your Java application enables seamless scaling, reliable message persistence, and improved decoupling between system components. Below is an example of a producer implementation following the **Modern Web App (MWA) pattern**, which refactors email delivery from a monolithic application into a standalone service.

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

        // Sends the email request to the outbound binding for Azure Service Bus.
        streamBridge.send("emailRequest-out-0", emailRequest);
    }
}
```

In this example, the `StreamBridge` abstracts the details of sending messages to Azure Service Bus, ensuring that your application can focus on business logic rather than messaging infrastructure.

## Case Study: Email ACA App and Queue-Based Load Leveling

In the reference implementation of the Modern Web App pattern, Contoso Fiber’s legacy Customer Account Management System (CAMS) is transformed to leverage cloud-native services. One key improvement is decoupling the email delivery functionality from the monolithic web app using Azure Service Bus.

### How It Works

- **Decoupled Email Requests:**  
  Email requests are enqueued to an Azure Service Bus queue rather than processed synchronously. This ensures that the web application remains responsive even during peak email activity.

- **Dedicated Email Processing Service:**  
  A standalone service (often implemented as an Azure Container App) monitors the Service Bus queue for new email requests. This service processes messages asynchronously, decoupling email delivery from the main application’s load.

- **Autoscaling Based on Queue Length:**  
  Using KEDA (Kubernetes-based Event Driven Autoscaling), the email processor automatically scales in response to the number of messages in the queue. This dynamic scaling embodies the core principle of Queue-Based Load Leveling.

### Benefits Realized

By leveraging Azure Service Bus for email processing, the Email ACA app benefits from:

- **Decoupling:**  
  The main application is insulated from delays or failures in email processing.
  
- **Scalability:**  
  The email processor scales dynamically to meet demand without overprovisioning resources.
  
- **Resilience:**  
  Built-in features such as message persistence, dead-letter queues, and automatic retries ensure robust operation.

## Conclusion

By integrating Azure Service Bus within the **Queue-Based Load Leveling pattern**, Java applications can achieve a more robust, scalable, and resilient architecture. This approach decouples producers from consumers, smooths out workload spikes, and ensures that the system remains responsive even during high-demand periods.

For a comprehensive guide and reference implementation, explore the **Modern Web App pattern for Java** on GitHub:  
[Modern Web App pattern for Java](https://github.com/Azure/modern-web-app-pattern-java)

## Further Reading

- [Official Azure Service Bus Java Documentation](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues)
- [Understanding Queue-Based Load Leveling](https://martinfowler.com/articles/queue-based-load-leveling.html)
- [Java Virtual Threads Overview (JEP 425)](https://openjdk.org/jeps/425)
