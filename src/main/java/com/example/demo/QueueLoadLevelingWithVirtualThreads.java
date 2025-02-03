package com.example.demo;

import javax.swing.SwingUtilities;
import java.util.concurrent.ExecutorService;

public class QueueLoadLevelingWithVirtualThreads {
    private static final int NUM_CONSUMERS = 2;

    public static void main(String[] args) {
        // Initialize the factory to create producers, consumers, status UI, and executor.
        EmailProcessingFactory factory = new EmailProcessingFactory();
        ExecutorService executor = factory.createExecutorService();

        // Launch the Swing-based status UI on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            factory.createStatusBarFrame().setVisible(true);
        });

        // Start consumer threads to process email tasks.
        for (int i = 1; i <= NUM_CONSUMERS; i++) {
            executor.execute(factory.createConsumer(i));
        }

        // Start a producer thread to generate email tasks.
        EmailProducer producer = factory.createProducer(1);
        executor.execute(producer);

        // Allow the simulation to run for a defined period.
        try {
            Thread.sleep(10000); // Run for 10 seconds.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Stop the producer and shut down all tasks.
        producer.stop();
        executor.shutdownNow();

        // Generate and display the final performance report.
        LangChainLLMReportGenerator reportGenerator = new LangChainLLMReportGenerator();
        System.out.println("Generating Report");
        String report = reportGenerator.generateReport(factory.getMonitor());
        System.out.println("Final Report: " + report);
    }
}