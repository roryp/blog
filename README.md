![email simulation](email.webp)

# Queue-Based Load Leveling in Java with Azure Service Bus

In today’s fast-paced software landscape, handling dynamic workloads efficiently is more critical than ever. One effective strategy is the **Queue-Based Load Leveling pattern**. This approach decouples task production from consumption by introducing a queue, smoothing out workload spikes and ensuring that both producers and consumers operate independently without overwhelming your system.

While in-memory queues might work well for smaller, monolithic applications, they quickly become a bottleneck as your application scales or adopts a distributed architecture. By integrating **Azure Service Bus**, you harness a scalable, robust, and durable messaging solution designed for modern cloud-native applications.

---

## What This Sample Demonstrates

This sample application showcases how to implement the Queue-Based Load Leveling pattern in Java, with several key components:

- **Email Simulation:**  
  Multiple producers generate email tasks—each assigned a unique ID—and enqueue them into a shared `BlockingQueue`. Consumers then process these tasks asynchronously. A "poison pill" mechanism ensures that consumer threads can terminate gracefully when the time comes.

- **Real-Time Status Dashboard:**  
  A Swing-based UI updates every 500 milliseconds, providing live metrics such as current queue length, total emails produced, processed counts, and the number of active processing threads.

- **LLM-Generated Performance Report:**  
  After the simulation runs its course, the application collects performance metrics and constructs a prompt for LangChain4j. Leveraging Ollama with the phi4 model, it generates a natural language report that succinctly summarizes the overall performance.

- **Modern Concurrency with Java 21:**  
  The application leverages Java 21’s virtual threads, ensuring efficient handling of concurrent tasks without the traditional overhead.

---

## Getting Started

### Prerequisites

- **Java 21:**  
  Required to take full advantage of virtual threads and the latest Java features.
- **Maven:**  
  For dependency management and building the project.
- **Ollama:**  
  Install [Ollama](https://ollama.com/) and set up the phi4 model using the following commands:
  ```bash
  ollama pull phi4
  ollama run phi4
  ```
  Ensure that the LLM server is accessible at `http://localhost:11434`.

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
   This command compiles your source code and produces an executable JAR file in the `target` directory.

### Running the Application

1. **Start the LLM Service:**  
   Make sure Ollama is up and running:
   ```bash
   ollama pull phi4
   ollama run phi4
   ```
2. **Launch the Application:**  
   Execute the following command to start the application:
   ```bash
   java -jar target/your-app.jar
   ```
   A Swing-based UI will appear, displaying live metrics. After a brief simulation period, an LLM-generated performance report will be output to the console.

---

## Leveraging Azure Service Bus for Scalable Messaging

In-memory queues are great for development and testing, but enterprise-level applications demand more. Azure Service Bus provides a powerful messaging backbone that offers:

- **Decoupling:**  
  By offloading message processing, your core application remains responsive even under heavy load.
- **Scalability:**  
  It dynamically adjusts to workload changes, reducing the need for overprovisioning resources.
- **Resilience:**  
  With features like message persistence, automatic retries, and dead-letter queues, your messaging system is built to withstand failures.

Below is a sample producer implementation that demonstrates how to integrate Azure Service Bus using the **Modern Web App (MWA) pattern**.

### Sample Producer Implementation

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

        // Sends the email request to the outbound binding targeting Azure Service Bus.
        streamBridge.send("emailRequest-out-0", emailRequest);
    }
}
```

In this snippet, the `StreamBridge` simplifies dispatching messages to Azure Service Bus, allowing your application to focus on its core business logic.

---

## Dedicated Email Processing with Azure Container Apps

For production environments, processing emails synchronously within the web application can be a recipe for disaster under load. Instead, by offloading email processing to Azure Service Bus, you can delegate this task to a dedicated service. This service, deployed via **Azure Container Apps**, continuously monitors the queue and processes messages asynchronously. 

Thanks to Kubernetes-based Event Driven Autoscaling (KEDA), resources can scale dynamically based on the queue length. This ensures that your email processing service remains robust and responsive, even during traffic surges.

## Real-World Application: Contoso Fiber and the MWA Pattern

Contoso Fiber, a Modern Web App (MWA) reference application, illustrates how a legacy Customer Account Management System (CAMS) can be effectively transformed using modern design principles. By adopting the Modern Web App pattern along with queue-based load leveling for its email service, Contoso Fiber achieves:

- **Enhanced Responsiveness:**  
  Email requests are queued for asynchronous handling rather than processed inline, keeping the main application fast and responsive even during peak load.

- **Independent Processing:**  
  A dedicated microservice manages email delivery separately, decoupling it from core workflows and optimizing resource use.

- **Dynamic Scaling:**  
  Utilizing Kubernetes-based Event Driven Autoscaling (KEDA), the email processing service automatically scales in response to demand fluctuations, ensuring robust performance.

Integrating Azure Service Bus with the Queue-Based Load Leveling pattern empowers Java applications with superior scalability, resilience, and efficiency. Decoupling critical operations and offloading them to containerized services like Azure Container Apps ensures that your system maintains high performance even under heavy workloads.

For further details and a reference implementation, explore the [Modern Web App pattern for Java](https://github.com/Azure/modern-web-app-pattern-java).

---

## Further Reading

- [Official Azure Service Bus Java Documentation](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues)
- [Understanding Queue-Based Load Leveling](https://martinfowler.com/articles/queue-based-load-leveling.html)
- [Modern Web App Pattern for Java](https://github.com/Azure/modern-web-app-pattern-java)
