package com.example.demo;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.ollama.OllamaStreamingLanguageModel;
import dev.langchain4j.model.output.Response;

public class LangChainLLMReportGenerator {

    public void generateReport(StatusMonitor monitor) {
        // Build a prompt with email processing metrics from the monitor
        String prompt = String.format(
            "Generate a final report on email processing metrics. Emails Produced: %d, Emails Processed: %d, Active Processing: %d. " +
            "Explain whether all emails were processed successfully or if there is a discrepancy, and summarize overall performance.",
            monitor.producedCount.get(), 
            monitor.processedCount.get(), 
            monitor.activeProcessingCount.get()
        );

        // Configure the Ollama streaming language model with a base URL, model name, and temperature
        OllamaStreamingLanguageModel model = OllamaStreamingLanguageModel.builder()
            .baseUrl("http://localhost:11434") // Set the URL for the API endpoint
            .modelName("phi4") // Specify the language model to use
            .temperature(0.7) // Set the randomness/creativity level for generated content
            .timeout(null)
            .build();

        // Generate report by streaming the response using a custom handler
        model.generate(prompt, new StreamingResponseHandler<String>() {

            // Called when a new token is received from the model
            @Override
            public void onNext(String token) {
                System.out.print(token);
            }
        
            // Called when the model finishes streaming the complete response
            @Override
            public void onComplete(Response<String> response) {
                System.out.println("onComplete: " + response);
            }
        
            // Called when an error occurs during the streaming process
            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
            }
        });
    }
}
