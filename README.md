# Queue-Based Load Leveling in Java with Azure Service Bus

In a world of dynamic workloads and ever-changing software landscapes, managing peaks in demand while maintaining high performance is essential. The **Queue-Based Load Leveling pattern** decouples task production from consumption by introducing an intermediary queue. This approach smooths out spikes in workload so that producers and consumers operate independently without overwhelming the system.

For smaller, monolithic applications, in-memory queues might be sufficient. However, as applications scale or become distributed, relying solely on in-memory solutions can fail. By integrating **Azure Service Bus** into your architecture, you embrace a scalable, robust, and durable messaging solution tailored to modern cloud applications.

## Overview

This sample application demonstrates how to implement this pattern in Java:

- **Email Simulation:**  
  Multiple producers generate email tasks, each assigned a unique ID and enqueued into a shared `BlockingQueue`. Consumers then process these tasks asynchronously. A "poison pill" mechanism gracefully terminates consumer threads when needed.

- **Real-Time Status UI:**  
  A Swing-based dashboard updates every 500 milliseconds to show key metrics including current queue length, total emails produced, emails processed, and active processing count.

- **LLM-Generated Performance Report:**  
  Once the simulation concludes, the application collects performance metrics and constructs a prompt for LangChain4j. Leveraging Ollama with the phi4 model, it generates a natural language report summarizing the overall performance.

- **Modern Concurrency:**  
  The application uses Java 21’s virtual threads, providing efficient handling of concurrent tasks.

## Getting Started

### Prerequisites

- **Java 21:** To utilize virtual threads and modern Java features.
- **Maven:** For dependency management and building the project.
- **Ollama:**  
  Install [Ollama](https://ollama.com/) and run the following commands to set up the phi4 model:
  ```bash
  ollama pull phi4
  ollama run phi4
  ```
  Ensure the LLM server is accessible at `http://localhost:11434`.

### Installation

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
   Run the following commands to ensure Ollama is ready:
   ```bash
   ollama pull phi4
   ollama run phi4
   ```
2. **Launch the Application:**  
   Execute the JAR file:
   ```bash
   java -jar target/your-app.jar
   ```
   The Swing-based UI will display live metrics. After a brief simulation period, an LLM-generated performance report will appear in the console.

## Leveraging Azure Service Bus for Scalable Messaging

While in-memory queues work well for testing or small applications, enterprise environments require the advanced features provided by Azure Service Bus. Its benefits include:

- **Decoupling:**  
  Keeps the core application responsive by offloading message processing.
- **Scalability:**  
  Dynamically adjusts to workload changes, eliminating the need for constant overprovisioning.
- **Resilience:**  
  Provides durability through message persistence, automatic retries, and dead-letter queues.

The integration of Azure Service Bus in your Java application not only enhances scalability but also abstracts the messaging infrastructure. Below is a sample producer implementation that follows the **Modern Web App (MWA) pattern**.

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

        // Sends the email request to the outbound binding targeting Azure Service Bus.
        streamBridge.send("emailRequest-out-0", emailRequest);
    }
}
```

In this example, the `StreamBridge` simplifies message dispatching to Azure Service Bus, letting your application concentrate on core business logic.

## Dedicated Email Processing with Azure Container Apps

For production environments, offloading email processing is critical. Instead of processing emails synchronously within the web application, they are enqueued to Azure Service Bus. A dedicated email processing service, deployed via **Azure Container Apps**, continuously monitors the queue and processes messages asynchronously. Kubernetes-based Event Driven Autoscaling (KEDA) dynamically adjusts resources based on the queue length to maintain high throughput and resilience even during demand surges.

This decoupled design ensures that the main application remains responsive while efficiently utilizing cloud resources.

## Case Study: Email ACA App and Queue-Based Load Leveling

Contoso Fiber transformed their legacy Customer Account Management System (CAMS) by implementing the Modern Web App pattern. Key benefits included:

- **Responsive Main Application:**  
  By enqueuing email requests instead of processing them inline, the application stayed robust under load.
- **Independent Processing:**  
  A dedicated service managed email delivery, enabling scalability and better resource utilization.
- **Dynamic Scaling:**  
  KEDA ensured the email processing service could scale dynamically in response to changing loads.

## Conclusion

Utilizing Azure Service Bus within the Queue-Based Load Leveling pattern empowers Java applications with robust, scalable, and resilient architecture. By decoupling critical operations and leveraging containerized services (e.g., Azure Container Apps), systems remain responsive even during peak demand. For more details and a reference implementation, explore the [Modern Web App pattern for Java](https://github.com/Azure/modern-web-app-pattern-java).

## Further Reading

- [Official Azure Service Bus Java Documentation](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues)
- [Understanding Queue-Based Load Leveling](https://martinfowler.com/articles/queue-based-load-leveling.html)
- [Modern Web App Pattern for Java](https://github.com/Azure/modern-web-app-pattern-java)
