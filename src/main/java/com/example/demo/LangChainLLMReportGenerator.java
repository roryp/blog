package com.example.demo;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class LangChainLLMReportGenerator {

    public String generateReport(StatusMonitor monitor) {
        String prompt = String.format(
            "Generate a final report on email processing metrics. Emails Produced: %d, Emails Processed: %d, Active Processing: %d. " +
            "Explain whether all emails were processed successfully or if there is a discrepancy, and summarize overall performance.",
            monitor.producedCount.get(), monitor.processedCount.get(), monitor.activeProcessingCount.get());

        ChatLanguageModel model = OllamaChatModel.builder()
            .baseUrl("http://localhost:11434")
            .modelName("phi4")
            .temperature(0.7)
            .build();

        return model.generate(prompt);
    }
}