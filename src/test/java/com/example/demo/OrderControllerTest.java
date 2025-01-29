package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import org.testcontainers.containers.BindMode;
import org.springframework.cloud.stream.function.StreamBridge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = DemoApplication.class)
public class OrderControllerTest {

    @Container
    public static GenericContainer<?> azureServiceBusEmulator = new GenericContainer<>("mcr.microsoft.com/azure-messaging/servicebus-emulator:1.0.1")
            .withExposedPorts(5672)
            .withFileSystemBind("./src/test/resources/service-bus-config.json", "/config/service-bus-config.json", BindMode.READ_ONLY)
            .withCommand("--config /config/service-bus-config.json")
            .waitingFor(Wait.forListeningPort());

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private StreamBridge streamBridge;

    @DynamicPropertySource
    static void registerAzureServiceBusProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.azure.servicebus.connection-string", () -> String.format(
            "Endpoint=sb://%s:%d/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=local-emulator-key",
            azureServiceBusEmulator.getHost(), azureServiceBusEmulator.getMappedPort(5672)
        ));
    }

    @Test
    public void testPlaceOrder() {
        // Verify that the Testcontainer is running
        assertThat(azureServiceBusEmulator.isRunning()).isTrue();

        // Mock the behavior of StreamBridge
        Order order = new Order("1", "Product A", 10, 100.0);
        when(streamBridge.send("output", order)).thenReturn(true);

        // Perform the API call
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Order> request = new HttpEntity<>(order, headers);
        ResponseEntity<String> response = restTemplate.exchange("/orders", HttpMethod.POST, request, String.class);

        // Verify response and StreamBridge interaction
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Order placed successfully");
        verify(streamBridge, times(1)).send("output", order);
    }
}
