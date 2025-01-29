package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerTest {

    @Container
    public static final GenericContainer<?> azureServiceBusEmulator = new GenericContainer<>("mcr.microsoft.com/azure-messaging/servicebus-emulator:1.0.1")
            .withExposedPorts(5672)
            .waitingFor(Wait.forListeningPort());

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void registerAzureServiceBusProperties(DynamicPropertyRegistry registry) {
        String host = azureServiceBusEmulator.getHost();
        int port = azureServiceBusEmulator.getMappedPort(5672);

        String serviceBusConnectionString = String.format(
            "Endpoint=sb://%s/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=your_access_key",
            host
        );

        registry.add("spring.cloud.azure.servicebus.connection-string", () -> serviceBusConnectionString);
    }

    @Test
    public void testPlaceOrder() {
        Order order = new Order();
        order.setId("1");
        order.setProduct("Product A");
        order.setQuantity(10);
        order.setPrice(100.0);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Order> request = new HttpEntity<>(order, headers);

        ResponseEntity<String> response = restTemplate.exchange("/orders", HttpMethod.POST, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Order placed successfully");
    }
}
