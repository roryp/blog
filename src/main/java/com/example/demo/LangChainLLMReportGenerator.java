package com.example.demo;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.ollama.OllamaStreamingLanguageModel;
import dev.langchain4j.model.output.Response;

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
