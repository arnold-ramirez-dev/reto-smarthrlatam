package com.arnold.alertoperations.alerts.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arnold.alertoperations.alerts.AlertService;
import com.arnold.alertoperations.alerts.AlertSeverity;
import com.arnold.alertoperations.alerts.AlertsController;
import com.arnold.alertoperations.alerts.ApiExceptionHandler;
import com.arnold.alertoperations.alerts.dto.AlertResponse;
import com.arnold.alertoperations.alerts.exceptions.PersistenceOperationException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AlertsController.class)
@Import({ApiExceptionHandler.class, AlertsControllerTest.TestClockConfiguration.class})
class AlertsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlertService alertService;

    @Test
    void returnsCreatedForValidAlert() throws Exception {
        AlertResponse response = new AlertResponse(
                UUID.fromString("1b0ef74d-6e53-41d0-a7a1-4f1a48c0f8ec"),
                "ORD-1001",
                AlertSeverity.HIGH,
                "Temperature exceeded threshold",
                "iot-monitor",
                Instant.parse("2026-11-10T14:22:00Z")
        );
        when(alertService.create(eq("ORD-1001"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders/ORD-1001/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"severity":"HIGH","message":"Temperature exceeded threshold","source":"iot-monitor"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ORD-1001"))
                .andExpect(jsonPath("$.severity").value("HIGH"));
    }

    @Test
    void returnsBadRequestForInvalidBody() throws Exception {
        mockMvc.perform(post("/api/v1/orders/ORD-1001/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"severity":"HIGH","message":"","source":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.message").exists())
                .andExpect(jsonPath("$.fieldErrors.source").exists());
    }

    @Test
    void returnsSanitizedServerErrorForDataFailure() throws Exception {
        when(alertService.create(eq("ORD-1001"), any()))
                .thenThrow(new PersistenceOperationException(new IllegalStateException("jdbc password should remain private")));

        mockMvc.perform(post("/api/v1/orders/ORD-1001/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"severity":"MEDIUM","message":"Retry exhausted","source":"scheduler"}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("PERSISTENCE_ERROR"))
                .andExpect(jsonPath("$.message").value("The request could not be completed"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("password"))));
    }

    @Test
    void returnsNotFoundForAnUnmappedStaticResource() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @TestConfiguration
    static class TestClockConfiguration {
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-11-10T14:22:00Z"), ZoneOffset.UTC);
        }
    }
}

