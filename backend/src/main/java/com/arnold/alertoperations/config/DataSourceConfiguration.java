package com.arnold.alertoperations.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Objects;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

@Configuration
@EnableConfigurationProperties(DatabaseConnectionProperties.class)
public class DataSourceConfiguration {

    @Bean
    public DataSource dataSource(DatabaseConnectionProperties properties, ObjectMapper objectMapper) {
        DatabaseCredentials credentials = StringUtils.hasText(properties.getSecretArn())
                ? loadCredentialsFromSecretsManager(properties, objectMapper)
                : new DatabaseCredentials(properties.getUrl(), properties.getUsername(), properties.getPassword());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(requireValue(credentials.url(), "database URL"));
        config.setUsername(credentials.username());
        config.setPassword(credentials.password());
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(3_000);
        config.setValidationTimeout(1_000);
        config.setPoolName("alert-operations-pool");
        return new HikariDataSource(config);
    }

    private DatabaseCredentials loadCredentialsFromSecretsManager(
            DatabaseConnectionProperties properties,
            ObjectMapper objectMapper
    ) {
        try (SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.of(properties.getAwsRegion()))
                .build()) {
            String secretJson = client.getSecretValue(GetSecretValueRequest.builder()
                    .secretId(properties.getSecretArn())
                    .build())
                    .secretString();
            JsonNode secret = objectMapper.readTree(secretJson);
            return new DatabaseCredentials(
                    requiredJsonValue(secret, "url"),
                    requiredJsonValue(secret, "username"),
                    requiredJsonValue(secret, "password")
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Database configuration could not be loaded from Secrets Manager", exception);
        }
    }

    private String requiredJsonValue(JsonNode node, String name) {
        JsonNode value = node.get(name);
        if (value == null || !StringUtils.hasText(value.asText())) {
            throw new IllegalStateException("Database secret is missing required field: " + name);
        }
        return value.asText();
    }

    private String requireValue(String value, String name) {
        return Objects.requireNonNull(value, name + " must be configured");
    }
}

