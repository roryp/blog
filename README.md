# Queue Load Leveling with Virtual Threads & LangChain4j

This project demonstrates a modern approach to managing fluctuating workloads in Java by combining lightweight concurrency via virtual threads with natural language report generation using LangChain4j. The application simulates email production and consumption, displays live system metrics via a Swing UI, and, upon simulation completion, generates a detailed performance report by querying a locally running LLM through the Ollama connector.

> **Tip:** Before running the LLM report generator, install and start your local LLM (Ollama) with the phi4 model:  
> ```bash
> ollama pull phi4
> ollama run phi4
> ```

## Overview

- **Email Simulation:**  
  Multiple producers generate email tasks (each with a unique ID) and place them in a shared `BlockingQueue`, while consumers process these tasks asynchronously. A "poison pill" mechanism gracefully terminates consumer threads.

- **Real-Time Status UI:**  
  A Swing-based dashboard updates every 500 ms to display the current queue length, total emails produced, emails processed, and active processing count.

- **LLM Report Generation:**  
  At the end of the simulation, the application builds a prompt from collected metrics and uses LangChain4j’s integration with Ollama (configured to use the *phi4* model) to generate a natural language report summarizing overall performance.

- **Modern Concurrency:**  
  Leverages Java 21 virtual threads to handle concurrent task execution efficiently.

## Getting Started

### Prerequisites

- **Java 21:** For virtual threads and modern language features.
- **Maven:** For dependency management and building the project.
- **Ollama:** Install [Ollama](https://ollama.com/) and run:
  ```bash
  ollama pull phi4
  ollama run phi4
  ```
  The LLM server should be accessible at `http://localhost:11434`.
- **LangChain4j Dependencies:** Ensure your `pom.xml` includes the necessary artifacts for LangChain4j and the OllamaChatModel.

### Installation

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/yourusername/QueueLoadLevelingWithVirtualThreads.git
   cd QueueLoadLevelingWithVirtualThreads
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
   The Swing UI will launch to display live metrics. After approximately 10 seconds, the simulation stops and an LLM-generated report is printed to the console.

### Docker (Optional)

To run the application in a container, build your Docker image and execute:
```bash
docker run -p 8080:8080 \
  -e LANGCHAIN4J_LOCALAI_CHAT_MODEL_BASE_URL=http://host.docker.internal:11434 \
  -e LANGCHAIN4J_LOCALAI_CHAT_MODEL_MODEL_NAME=phi4 \
  -e LANGCHAIN4J_LOCALAI_CHAT_MODEL_TEMPERATURE=0.7 \
  my-langchain4j-app:latest
```
This maps the container’s port 8080 to your host and passes the necessary environment variables.

---

# Queue-Based Load Leveling in Java with Azure Service Bus

In today’s dynamic software landscape, managing fluctuating workloads is essential for maintaining both performance and reliability. The **Queue-Based Load Leveling pattern** decouples task production from consumption by introducing an intermediary queue. This design smooths out demand spikes, ensuring that producers and consumers can operate independently without overwhelming the system.

While in-memory queues work well for small-scale or single-node applications, they often fall short in distributed or enterprise environments. By integrating **Azure Service Bus**, you can leverage a scalable, durable messaging solution that meets the demands of modern cloud applications.

## Overview

- **Decoupling:** Separates the production of tasks from their consumption, ensuring smoother performance under variable loads.
- **Scalability:** Handles workload spikes by offloading messages to a queue, allowing producers and consumers to scale independently.
- **Fault Tolerance:** Provides resilience against failures with features such as message persistence and dead-letter queues.
- **Modern Concurrency:** Utilizes Java virtual threads for lightweight concurrency, preparing your application for high throughput.

## Features

- **Auto-Scaling:** Azure Service Bus automatically adjusts to variable workloads.
- **Flexible Provisioning:** Advanced management of topics and queues supports complex messaging scenarios.
- **Fault Tolerance:** Built-in mechanisms handle transient failures through message persistence and dead-letter queues.

## Prerequisites

- **Java 17+** – Required for virtual threads and modern language features.
- **Maven/Gradle** – For building and running the project.
- (Optional) **Azure Subscription** – For testing Azure Service Bus integration.
- Familiarity with basic Java concurrency and messaging patterns.

## Quick Start: Virtual Threads with In-Memory Queue

The following example demonstrates a simple simulation of the Queue-Based Load Leveling pattern using Java’s virtual threads and an in-memory `BlockingQueue`. This sample shows how multiple producers and consumers coordinate via a shared queue.

For the complete source code, see: [QueueLoadLevelingWithVirtualThreads.java](src/main/java/com/example/demo/QueueLoadLevelingWithVirtualThreads.java).

```java
// Represents an email to be processed.
record EmailTask(int id, String content) {}

// A producer class simulating email creation and enqueuing.
class EmailProducer implements Runnable {
    // Implementation details...
}
```

**How It Works:**

- **Producers:** Generate email tasks and place them into a shared `BlockingQueue`.
- **Consumers:** Retrieve and process tasks asynchronously.
- **Virtual Threads:** Provide efficient concurrent processing with minimal overhead.

This in-memory approach is ideal for demonstration purposes and smaller applications.

## Comparison with Azure Service Bus

While in-memory queues are great for local testing, enterprise applications benefit from a fully managed messaging service like **Azure Service Bus**. Its advantages include:

- **Decoupling:** Insulates the main application from delays in processing.
- **Scalability:** Dynamically scales based on workload without overprovisioning.
- **Resilience:** Ensures robust operation through message persistence and automatic retries.

For an in-depth reference, check out the [Modern Web App pattern for Java](https://github.com/Azure/modern-web-app-pattern-java).

## Implementing Queue-Based Load Leveling with Azure Service Bus

Integrating Azure Service Bus into your Java application enables seamless scaling, reliable message persistence, and improved decoupling. Below is an example of a producer implementation following the **Modern Web App (MWA) pattern**.

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

In this example, the `StreamBridge` abstracts the details of sending messages to Azure Service Bus, allowing your application to focus on business logic rather than messaging infrastructure.

## Case Study: Email ACA App and Queue-Based Load Leveling

In the Modern Web App pattern, Contoso Fiber transformed its legacy Customer Account Management System (CAMS) by decoupling email delivery from the monolithic app. Key improvements include:

- **Decoupled Email Requests:** Email requests are enqueued instead of processed synchronously, ensuring responsiveness.
- **Dedicated Email Processing Service:** A standalone service monitors the queue and processes messages asynchronously.
- **Autoscaling Based on Queue Length:** Using KEDA (Kubernetes-based Event Driven Autoscaling), the email processor scales dynamically according to queue depth.

### Benefits Realized

- **Decoupling:** The main application remains unaffected by email processing delays.
- **Scalability:** Dynamic scaling meets demand without resource overprovisioning.
- **Resilience:** Features like message persistence and automatic retries enhance overall robustness.

## Conclusion

Integrating Azure Service Bus within the Queue-Based Load Leveling pattern allows Java applications to achieve a robust, scalable, and resilient architecture. This approach decouples producers and consumers, smooths out workload spikes, and keeps the system responsive during high-demand periods.

For a comprehensive guide and reference implementation, explore the [Modern Web App pattern for Java](https://github.com/Azure/modern-web-app-pattern-java).

## Further Reading

- [Official Azure Service Bus Java Documentation](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues)
- [Understanding Queue-Based Load Leveling](https://martinfowler.com/articles/queue-based-load-leveling.html)
- [Java Virtual Threads Overview (JEP 425)](https://openjdk.org/jeps/425)

---

## Project Structure

- **StatusMonitor.java:**  
  Tracks email production, processing, and active processing counts using atomic counters.

- **EmailProducer.java & EmailConsumer.java:**  
  Simulate asynchronous production and consumption of emails using a `BlockingQueue`.

- **StatusBarFrame.java:**  
  A Swing-based dashboard that displays real-time system metrics.

- **LangChainLLMReportGenerator.java:**  
  Uses LangChain4j’s OllamaChatModel to generate a detailed performance report based on the collected metrics.

- **QueueLoadLevelingWithVirtualThreads.java (Main Class):**  
  Orchestrates the simulation, manages virtual threads, and triggers the report generation.