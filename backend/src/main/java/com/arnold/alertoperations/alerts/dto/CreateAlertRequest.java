package com.arnold.alertoperations.alerts.dto;

import com.arnold.alertoperations.alerts.AlertSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAlertRequest(
        @NotNull(message = "severity is required") AlertSeverity severity,
        @NotBlank(message = "message is required") @Size(max = 500, message = "message must not exceed 500 characters") String message,
        @NotBlank(message = "source is required") @Size(max = 100, message = "source must not exceed 100 characters") String source
) {
}

