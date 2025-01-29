package com.example.demo;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

public class Main {
    private static final String CONNECTION_STRING = "Endpoint=sb://127.0.0.1;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;" ;
    private static final String QUEUE_NAME = "queue.1";

    public static void main(String[] args) {
        // Create a ServiceBusClientBuilder
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(CONNECTION_STRING);

        // Sending a message
        ServiceBusSenderClient senderClient = builder
            .sender()
            .queueName(QUEUE_NAME)
            .buildClient();

        String message = "Hello, Service Bus!";
        senderClient.sendMessage(new ServiceBusMessage(message));
        System.out.println("Sent : " + message);

        // Receiving a message
        ServiceBusReceiverClient receiverClient = builder
            .receiver()
            .queueName(QUEUE_NAME)
            .buildClient();

        receiverClient.receiveMessages(1).forEach(msg -> {
            System.out.println("Received: " + msg.getBody().toString());
            receiverClient.complete(msg);
        });

        // Close the clients
        senderClient.close();
        receiverClient.close();
    }
}