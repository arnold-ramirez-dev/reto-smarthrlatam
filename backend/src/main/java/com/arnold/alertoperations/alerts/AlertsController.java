package com.arnold.alertoperations.alerts;

import com.arnold.alertoperations.alerts.dto.AlertResponse;
import com.arnold.alertoperations.alerts.dto.CreateAlertRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/orders/{orderId}/alerts")
public class AlertsController {

    private final AlertService alertService;

    public AlertsController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertResponse create(
            @PathVariable @NotBlank @Size(max = 64) String orderId,
            @Valid @RequestBody CreateAlertRequest request
    ) {
        return alertService.create(orderId, request);
    }

    @GetMapping
    public List<AlertResponse> list(
            @PathVariable @NotBlank @Size(max = 64) String orderId,
            @RequestParam(required = false) AlertSeverity severity
    ) {
        return alertService.list(orderId, severity);
    }
}

