package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Autowired
    private StreamBridge streamBridge;

    @PostMapping("/orders")
    public String placeOrder(@RequestBody Order order) {
        // Send the order event to the Azure Service Bus queue
        streamBridge.send("orders-out-0", MessageBuilder.withPayload(order).build());
        return "Order placed successfully";
    }
}
