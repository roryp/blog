# Queue-Based Load Leveling in Java with Azure Service Bus

In today’s dynamic software landscape, managing fluctuating workloads is crucial for ensuring high performance and reliability. The **Queue-Based Load Leveling pattern** decouples task production from consumption by inserting an intermediary queue. This design smooths out demand spikes so that producers and consumers operate independently without overwhelming the system.

While in-memory queues are ideal for small-scale or single-node applications, they often fall short in distributed or enterprise environments. By integrating **Azure Service Bus**, you can take advantage of a scalable, durable messaging solution that meets modern cloud application demands. This sample application simulates email production and consumption, displays live system metrics via a Swing UI, and—upon simulation completion—generates a detailed performance report using LangChain4j with an Ollama connector.

> **Tip:** Before running the LLM report generator, install and start your local LLM (Ollama) with the phi4 model:  
> ```bash
> ollama pull phi4
> ollama run phi4
> ```

---

## Overview

- **Email Simulation:**  
  Multiple producers generate email tasks (each with a unique ID) and enqueue them into a shared `BlockingQueue`, while consumers process these tasks asynchronously. A "poison pill" mechanism gracefully terminates consumer threads.

- **Real-Time Status UI:**  
  A Swing-based dashboard updates every 500 ms to display the current queue length, total emails produced, emails processed, and active processing count.

- **LLM Report Generation:**  
  After the simulation ends, the application builds a prompt from collected metrics and uses LangChain4j’s integration with Ollama (configured to use the *phi4* model) to generate a natural language report summarizing overall performance.

- **Modern Concurrency:**  
  Leverages Java 21 virtual threads to efficiently handle concurrent task execution.

---

## Getting Started

### Prerequisites

- **Java 21:** For virtual threads and modern language features.
- **Maven:** For dependency management and building the project.
- **Ollama:**  
  Install [Ollama](https://ollama.com/) and run:
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
   This command compiles the source code and generates an executable JAR in the `target` directory.

### Running the Application

1. **Start the LLM Service:**  
   Make sure Ollama is running the phi4 model:
   ```bash
   ollama pull phi4
   ollama run phi4
   ```

2. **Run the Application:**  
   Execute the JAR file:
   ```bash
   java -jar target/your-app.jar
   ```
   The Swing UI will launch and display live metrics. After a few seconds, the simulation stops and an LLM-generated report is printed to the console.

### Docker (Optional)

To run the application in a container, build your Docker image and execute:
```bash
docker run -p 8080:8080 \
  -e LANGCHAIN4J_LOCALAI_CHAT_MODEL_BASE_URL=http://host.docker.internal:11434 \
  -e LANGCHAIN4J_LOCALAI_CHAT_MODEL_MODEL_NAME=phi4 \
  -e LANGCHAIN4J_LOCALAI_CHAT_MODEL_TEMPERATURE=0.7 \
  my-langchain4j-app:latest
```
This maps the container’s port 8080 to your host and supplies the necessary environment variables.

---

## Comparison with Azure Service Bus

In-memory queues work well for local testing, but enterprise applications benefit from using a fully managed messaging service like **Azure Service Bus**. Its advantages include:

- **Decoupling:**  
  Insulates the main application from processing delays.

- **Scalability:**  
  Dynamically scales to handle variable workloads without overprovisioning resources.

- **Resilience:**  
  Offers robust features such as message persistence, automatic retries, and dead-letter queues.

For an in-depth reference, check out the [Modern Web App pattern for Java](https://github.com/Azure/modern-web-app-pattern-java).

---

## Implementing Queue-Based Load Leveling with Azure Service Bus

Integrating Azure Service Bus into your Java application enhances scalability, message persistence, and decoupling. The following example demonstrates a producer implementation that adheres to the **Modern Web App (MWA) pattern**.

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

In this example, `StreamBridge` abstracts the details of sending messages to Azure Service Bus, allowing your application to focus on business logic rather than messaging infrastructure.

---

## Dedicated Email Processing with Azure Container Apps

One of the key benefits of the Modern Web App pattern is its ability to decouple application components for improved scalability and resilience. In a production environment, the email processing functionality is ideally implemented as a dedicated service deployed via **Azure Container Apps**.

### How It Works

- **Decoupling from the Main Application:**  
  Email requests are enqueued to Azure Service Bus rather than processed directly by the web application. This decoupling ensures that the web app remains responsive even during peak loads.

- **Containerized Email Processor:**  
  The email processing service runs as an independent Azure Container App. This service monitors the Azure Service Bus queue for new email requests and processes them asynchronously.

- **Autoscaling with KEDA:**  
  Azure Container Apps integrate with Kubernetes-based Event Driven Autoscaling (KEDA) to dynamically scale the number of container instances based on the queue length. When the queue depth increases, additional containers spin up to handle the load, ensuring timely processing and robust performance.

### Benefits of the Modern Web App Pattern

- **Improved Responsiveness:**  
  By offloading email processing to a dedicated, containerized service, the main application avoids bottlenecks and remains highly responsive.
  
- **Dynamic Scalability:**  
  Autoscaling via KEDA allows the email processor to adjust resources dynamically in response to workload changes, ensuring efficient use of cloud resources.
  
- **Enhanced Resilience:**  
  With Azure Service Bus handling message persistence and retries, combined with the isolation of containerized services, the system is well-prepared to handle transient failures and spikes in demand.

For a comprehensive guide and reference implementation, see the [Modern Web App pattern for Java](https://github.com/Azure/modern-web-app-pattern-java).

---

## Case Study: Email ACA App and Queue-Based Load Leveling

In the Modern Web App pattern, Contoso Fiber transformed its legacy Customer Account Management System (CAMS) by decoupling email delivery from the monolithic app. Key improvements include:

- **Decoupled Email Requests:**  
  Email requests are enqueued instead of processed synchronously, keeping the main application responsive.

- **Dedicated Email Processing Service:**  
  A standalone service (deployed as an Azure Container App) monitors the Azure Service Bus queue and processes messages asynchronously.

- **Autoscaling Based on Queue Length:**  
  Leveraging KEDA, the email processor scales dynamically based on queue depth, ensuring high throughput during demand spikes.

### Benefits Realized

- **Decoupling:**  
  The main application remains unaffected by delays or failures in email processing.

- **Scalability:**  
  Dynamic autoscaling ensures that resources are efficiently allocated according to demand.

- **Resilience:**  
  Robust messaging features (persistence, retries, dead-lettering) combined with containerized services enhance overall system reliability.

---

## Conclusion

Integrating Azure Service Bus within the Queue-Based Load Leveling pattern enables Java applications to achieve robust, scalable, and resilient architectures. The Modern Web App pattern—by decoupling core functionalities and deploying services as independent containerized applications (such as via Azure Container Apps)—ensures that the system remains responsive even during high-demand periods.

For more details and a reference implementation, explore the [Modern Web App pattern for Java](https://github.com/Azure/modern-web-app-pattern-java).

---

## Further Reading

- [Official Azure Service Bus Java Documentation](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues)
- [Understanding Queue-Based Load Leveling](https://martinfowler.com/articles/queue-based-load-leveling.html)