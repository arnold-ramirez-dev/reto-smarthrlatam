package com.arnold.alertoperations.alerts.dto;

import com.arnold.alertoperations.alerts.AlertSeverity;
import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        String orderId,
        AlertSeverity severity,
        String message,
        String source,
        Instant createdAt
) {
}

