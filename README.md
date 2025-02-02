Today, we’re exploring **Queue-Based Load Leveling**—a powerful pattern for smoothing out workload spikes—using Java and Azure Service Bus. If you’re building scalable, resilient applications, read on!

![email simulation](email.webp)

## Why Queue-Based Load Leveling?

In our fast-paced software world, dynamic workloads are the norm. The **Queue-Based Load Leveling pattern** helps decouple the production and consumption of tasks by introducing a queue between them. This means that your producers and consumers work independently, preventing your system from getting overwhelmed during traffic spikes.

For small, monolithic applications, in-memory queues might suffice. But as your application scales or adopts a distributed architecture, these simple queues can quickly become a bottleneck. That’s where **Azure Service Bus** steps in. This robust, cloud-native messaging solution provides the scalability, durability, and resilience that modern applications demand.

---

## What Does This Sample Do?

This sample application is a playground for implementing the Queue-Based Load Leveling pattern in Java. Here’s what you’ll see:

- **Email Simulation:**  
  Multiple producers generate email tasks—each with a unique ID—and enqueue them into a shared `BlockingQueue`. On the other side, consumers process these tasks asynchronously. A “poison pill” mechanism ensures that consumer threads terminate gracefully when their work is done.

- **Real-Time Status Dashboard:**  
  A sleek Swing-based UI updates every 500 milliseconds, showing live metrics like current queue length, total emails produced, processed counts, and the number of active threads. It’s a great way to visualize your system’s heartbeat.

- **LLM-Generated Performance Report:**  
  At the end of the simulation, the application gathers performance metrics and crafts a prompt for LangChain4j. Using Ollama with the phi4 model, it then produces a natural language report summarizing overall performance. Think of it as a performance review for your system!

- **Modern Concurrency with Java 21:**  
  With Java 21’s virtual threads, the application handles concurrency efficiently, cutting down on the traditional overhead and making your code both cleaner and faster.

---

## Getting Started

### Prerequisites

Before diving in, ensure you have:

- **Java 21:**  
  Leverage the latest Java features, including virtual threads.
- **Maven:**  
  Manage dependencies and build the project seamlessly.
- **Ollama:**  
  Install [Ollama](https://ollama.com/) and set up the phi4 model:
  ```bash
  ollama pull phi4
  ollama run phi4
  ```
  Make sure the LLM server is accessible at `http://localhost:11434`.

### Installation Steps

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/roryp/blog
   cd blog
   ```
2. **Build the Project:**
   ```bash
   mvn clean package
   ```
   This compiles your source code and produces an executable JAR in the `target` directory.

### Running the Application

1. **Start the LLM Service:**  
   Ensure Ollama is running:
   ```bash
   ollama pull phi4
   ollama run phi4
   ```
2. **Launch the Application:**  
   Start the application with:
   ```bash
   java -jar target/your-app.jar
   ```
   A Swing-based UI will appear with live metrics, and after a brief simulation, an LLM-generated performance report will be displayed in your console.

---

## Azure Service Bus: The Messaging Backbone for Scalability

The basic example does a lot but still suffers from coupling, scalability, and resilience issues.
While in-memory queues are great for prototyping, enterprise-grade applications need a messaging backbone that can handle heavy loads. **Azure Service Bus** is exactly that—it decouples message processing from your core application logic, ensuring responsiveness even under high traffic. Here’s why it’s a game changer:

- **Decoupling:**  
  Offload message processing to keep your main application responsive.
- **Scalability:**  
  Automatically adjust to workload changes without overprovisioning.
- **Resilience:**  
  Features like message persistence, automatic retries, and dead-letter queues ensure your system can handle failures gracefully.

### A Glimpse at the Producer Code

Below is a snippet demonstrating how to integrate Azure Service Bus in your Java application using Spring Cloud Stream:

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

        // Dispatch the email request to Azure Service Bus via the outbound binding.
        streamBridge.send("emailRequest-out-0", emailRequest);
    }
}
```

Here, the StreamBridge abstracts the complexity of interacting with Azure Service Bus, letting you concentrate on your core business logic instead of managing low-level messaging operations. Additionally, rather than processing emails synchronously and risking performance bottlenecks during high-traffic periods, this design offloads email handling to a dedicated service. Deployed via **Azure Container Apps**, the service continuously monitors the queue and processes messages asynchronously, ensuring reliable and scalable email management under heavy load.

With Kubernetes-based Event Driven Autoscaling (KEDA), the system scales resources dynamically based on the queue length, ensuring that your email service remains robust even during peak traffic. For example, Contoso Fiber—a Modern Web App (MWA) reference application—transforms its legacy Customer Account Management System (CAMS) by adopting these modern patterns. The benefits include:

- **Enhanced Responsiveness:**  
  By queuing email requests for asynchronous processing, the main application stays fast and responsive.
- **Independent Processing:**  
  A dedicated microservice handles email delivery, decoupling it from core workflows and optimizing resource usage.
- **Dynamic Scaling:**  
  KEDA automatically scales the service in response to demand, ensuring robust performance during traffic surges.

Integrating Azure Service Bus with queue-based load leveling empowers your Java applications with unparalleled scalability and resilience. Offloading critical operations to containerized services like Azure Container Apps keeps your system performing at its best, even under heavy loads.
Embracing modern patterns like queue-based load leveling is key to building scalable, resilient applications. Integrating tools like Azure Service Bus into your Java projects can transform the way your systems handle load and scale.

## Further Reading

- [Official Azure Service Bus Java Documentation](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues)
- [Understanding Queue-Based Load Leveling](https://martinfowler.com/articles/queue-based-load-leveling.html)
- [Modern Web App Pattern for Java](https://github.com/Azure/modern-web-app-pattern-java)

---