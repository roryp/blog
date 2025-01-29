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
        // Ensure the emulator is running
        assertThat(azureServiceBusEmulator.isRunning()).isTrue();

        Order order = new Order("1", "Product A", 10, 100.0);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Order> request = new HttpEntity<>(order, headers);

        // Mock StreamBridge to return true when sending messages
        when(streamBridge.send("output", order)).thenReturn(true);

        ResponseEntity<String> response = restTemplate.exchange("/orders", HttpMethod.POST, request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Order placed successfully");

        // Verify that StreamBridge was called
        verify(streamBridge, times(1)).send("output", order);
    }
}
