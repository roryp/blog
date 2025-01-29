package com.example.demo;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final StreamBridge streamBridge;

    public OrderController(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @PostMapping
    public String placeOrder(@RequestBody Order order) {
        // ✅ Send order to Azure Service Bus Queue
        boolean sent = streamBridge.send("output", order);

        if (!sent) {
            throw new RuntimeException("Failed to send message");
        }
        return "Order placed successfully";
    }
}
