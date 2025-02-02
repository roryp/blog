package com.example.demo;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

class StatusMonitor {
    public final AtomicInteger producedCount = new AtomicInteger(0);
    public final AtomicInteger processedCount = new AtomicInteger(0);
    public final AtomicInteger activeProcessingCount = new AtomicInteger(0);
}

record EmailTask(int id, String content) {}

class EmailProducer implements Runnable {
    private static final AtomicInteger emailIdCounter = new AtomicInteger(0);
    private final BlockingQueue<EmailTask> queue;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final int producerId;
    private final StatusMonitor monitor;

    public EmailProducer(BlockingQueue<EmailTask> queue, int producerId, StatusMonitor monitor) {
        this.queue = queue;
        this.producerId = producerId;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Producer-" + producerId);
        try {
            while (running.get()) {
                int emailId = emailIdCounter.incrementAndGet();
                EmailTask email = new EmailTask(emailId, "Email " + emailId + " from Producer " + producerId);
                queue.put(email);
                monitor.producedCount.incrementAndGet();
                Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println("Producer " + producerId + " stopped.");
        }
    }

    public void stop() {
        running.set(false);
    }
}

class EmailConsumer implements Runnable {
    private final BlockingQueue<EmailTask> queue;
    private final int consumerId;
    private final StatusMonitor monitor;

    public EmailConsumer(BlockingQueue<EmailTask> queue, int consumerId, StatusMonitor monitor) {
        this.queue = queue;
        this.consumerId = consumerId;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Consumer-" + consumerId);
        try {
            while (true) {
                EmailTask email = queue.take();
                if (email.id() == -1) {
                    queue.put(email);
                    break;
                }
                monitor.activeProcessingCount.incrementAndGet();
                Thread.sleep(ThreadLocalRandom.current().nextInt(200, 1000));
                monitor.activeProcessingCount.decrementAndGet();
                monitor.processedCount.incrementAndGet();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println("Consumer " + consumerId + " stopped.");
        }
    }
}

class StatusBarFrame extends JFrame {
    private final JLabel queueLabel;
    private final JLabel producedLabel;
    private final JLabel processedLabel;
    private final JLabel activeLabel;
    private final JProgressBar progressBar;
    private final BlockingQueue<EmailTask> queue;
    private final StatusMonitor monitor;
    private final Timer timer;

    public StatusBarFrame(BlockingQueue<EmailTask> queue, StatusMonitor monitor) {
        super("Queue Load Leveling Status");
        this.queue = queue;
        this.monitor = monitor;

        queueLabel = new JLabel("Queue Length: 0");
        producedLabel = new JLabel("Emails Produced: 0");
        processedLabel = new JLabel("Emails Processed: 0");
        activeLabel = new JLabel("Active Processing: 0");
        progressBar = new JProgressBar(0, 100);

        Font font = new Font("SansSerif", Font.BOLD, 16);
        queueLabel.setFont(font);
        producedLabel.setFont(font);
        processedLabel.setFont(font);
        activeLabel.setFont(font);
        progressBar.setFont(font);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(queueLabel);
        panel.add(producedLabel);
        panel.add(processedLabel);
        panel.add(activeLabel);
        panel.add(progressBar);

        add(panel, BorderLayout.CENTER);
        timer = new Timer(500, e -> updateStatus());
        timer.start();

        setSize(400, 200);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void updateStatus() {
        queueLabel.setText("Queue Length: " + queue.size());
        producedLabel.setText("Emails Produced: " + monitor.producedCount.get());
        processedLabel.setText("Emails Processed: " + monitor.processedCount.get());
        activeLabel.setText("Active Processing: " + monitor.activeProcessingCount.get());
        progressBar.setValue((int) ((double) monitor.processedCount.get() / monitor.producedCount.get() * 100));
    }
}

class LangChainLLMReportGenerator {
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

public class QueueLoadLevelingWithVirtualThreads {
    private static final int NUM_CONSUMERS = 2;

    public static void main(String[] args) {
        BlockingQueue<EmailTask> queue = new LinkedBlockingQueue<>();
        StatusMonitor monitor = new StatusMonitor();

        SwingUtilities.invokeLater(() -> {
            StatusBarFrame frame = new StatusBarFrame(queue, monitor);
            frame.setVisible(true);
        });

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            EmailProducer producer1 = new EmailProducer(queue, 1, monitor);
            EmailProducer producer2 = new EmailProducer(queue, 2, monitor);
            var producerFuture1 = executor.submit(producer1);
            var producerFuture2 = executor.submit(producer2);

            EmailConsumer consumer1 = new EmailConsumer(queue, 1, monitor);
            EmailConsumer consumer2 = new EmailConsumer(queue, 2, monitor);
            var consumerFuture1 = executor.submit(consumer1);
            var consumerFuture2 = executor.submit(consumer2);

            Thread.sleep(10_000);

            System.out.println("Initiating shutdown of producers...");
            producer1.stop();
            producer2.stop();
            try {
                producerFuture1.get();
                producerFuture2.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            System.out.println("Inserting poison pills for consumers...");
            for (int i = 0; i < NUM_CONSUMERS; i++) {
                queue.put(new EmailTask(-1, "POISON"));
            }

            try {
                consumerFuture1.get();
                consumerFuture2.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            System.out.println("Shutdown complete.");

            try {
                LangChainLLMReportGenerator reportGenerator = new LangChainLLMReportGenerator();
                String finalReport = reportGenerator.generateReport(monitor);
                System.out.println("=== LLM Generated Report ===");
                System.out.println(finalReport);
            } catch (Exception e) {
                System.err.println("Error generating LLM report: " + e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
