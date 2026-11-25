package com.arnold.alertoperations.alerts;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByOrder_IdOrderByCreatedAtDesc(String orderId);

    List<Alert> findByOrder_IdAndSeverityOrderByCreatedAtDesc(String orderId, AlertSeverity severity);
}

