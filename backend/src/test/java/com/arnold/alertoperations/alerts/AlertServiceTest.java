package com.arnold.alertoperations.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.arnold.alertoperations.alerts.dto.AlertResponse;
import com.arnold.alertoperations.alerts.dto.CreateAlertRequest;
import com.arnold.alertoperations.alerts.exceptions.OrderNotFoundException;
import com.arnold.alertoperations.alerts.exceptions.PersistenceOperationException;
import com.arnold.alertoperations.orders.OrderEntity;
import com.arnold.alertoperations.orders.OrderRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-10T14:22:00Z");

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private OrderRepository orderRepository;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(alertRepository, orderRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsAlertForExistingOrder() {
        OrderEntity order = new OrderEntity("ORD-1001", NOW);
        CreateAlertRequest request = new CreateAlertRequest(AlertSeverity.HIGH, "Temperature exceeded threshold", "iot-monitor");
        when(orderRepository.findById("ORD-1001")).thenReturn(Optional.of(order));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlertResponse response = alertService.create("ORD-1001", request);

        assertEquals("ORD-1001", response.orderId());
        assertEquals(AlertSeverity.HIGH, response.severity());
        assertEquals("Temperature exceeded threshold", response.message());
        assertEquals(NOW, response.createdAt());
    }

    @Test
    void rejectsUnknownOrder() {
        when(orderRepository.findById("ORD-404")).thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> alertService.create("ORD-404", new CreateAlertRequest(AlertSeverity.LOW, "Check sensor", "monitor"))
        );
    }

    @Test
    void wrapsDataLayerFailureInControlledException() {
        OrderEntity order = new OrderEntity("ORD-1001", NOW);
        when(orderRepository.findById("ORD-1001")).thenReturn(Optional.of(order));
        when(alertRepository.save(any(Alert.class))).thenThrow(new DataAccessResourceFailureException("database unavailable"));

        assertThrows(
                PersistenceOperationException.class,
                () -> alertService.create("ORD-1001", new CreateAlertRequest(AlertSeverity.MEDIUM, "Retry exhausted", "scheduler"))
        );
    }
}

