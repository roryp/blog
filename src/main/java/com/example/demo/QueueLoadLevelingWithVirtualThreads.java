package com.example.demo;

import javax.swing.SwingUtilities;
import java.util.concurrent.ExecutorService;

public class QueueLoadLevelingWithVirtualThreads {
    private static final int NUM_CONSUMERS = 2;
    public static void main(String[] args) {
        EmailProcessingFactory factory = new EmailProcessingFactory();
        ExecutorService executor = factory.createExecutorService();

        SwingUtilities.invokeLater(() -> {
            factory.createStatusBarFrame().setVisible(true);
        });

        // Start consumers.
        for (int i = 1; i <= NUM_CONSUMERS; i++) {
            executor.execute(factory.createConsumer(i));
        }

        // Start a producer.
        EmailProducer producer = factory.createProducer(1);
        executor.execute(producer);

        // Let the simulation run for a while.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        producer.stop();
        executor.shutdownNow();

        // Generate and print a final report.
        LangChainLLMReportGenerator reportGenerator = new LangChainLLMReportGenerator();
        System.out.println("Generating Report");
        String report = reportGenerator.generateReport(factory.getMonitor());
        System.out.println("Final Report: " + report);
    }
}