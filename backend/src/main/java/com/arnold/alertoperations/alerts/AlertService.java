package com.arnold.alertoperations.alerts;

import com.arnold.alertoperations.alerts.dto.AlertResponse;
import com.arnold.alertoperations.alerts.dto.CreateAlertRequest;
import com.arnold.alertoperations.alerts.exceptions.OrderNotFoundException;
import com.arnold.alertoperations.alerts.exceptions.PersistenceOperationException;
import com.arnold.alertoperations.orders.OrderEntity;
import com.arnold.alertoperations.orders.OrderRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final OrderRepository orderRepository;
    private final Clock clock;

    public AlertService(AlertRepository alertRepository, OrderRepository orderRepository, Clock clock) {
        this.alertRepository = alertRepository;
        this.orderRepository = orderRepository;
        this.clock = clock;
    }

    @Transactional
    public AlertResponse create(String orderId, CreateAlertRequest request) {
        try {
            OrderEntity order = findExistingOrder(orderId);
            Alert alert = new Alert(
                    order,
                    request.severity(),
                    request.message().trim(),
                    request.source().trim(),
                    Instant.now(clock)
            );
            return toResponse(alertRepository.save(alert));
        } catch (DataAccessException exception) {
            throw new PersistenceOperationException(exception);
        }
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> list(String orderId, AlertSeverity severity) {
        try {
            findExistingOrder(orderId);
            List<Alert> alerts = severity == null
                    ? alertRepository.findByOrder_IdOrderByCreatedAtDesc(orderId)
                    : alertRepository.findByOrder_IdAndSeverityOrderByCreatedAtDesc(orderId, severity);
            return alerts.stream().map(this::toResponse).toList();
        } catch (DataAccessException exception) {
            throw new PersistenceOperationException(exception);
        }
    }

    private OrderEntity findExistingOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getOrder().getId(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.getSource(),
                alert.getCreatedAt()
        );
    }
}

