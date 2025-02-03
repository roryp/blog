package com.example.demo;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.BlockingQueue;

public class StatusBarFrame extends JFrame {

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
        int produced = monitor.producedCount.get();
        progressBar.setValue(produced == 0 ? 0 : (int)((double) monitor.processedCount.get() / produced * 100));
    }
}