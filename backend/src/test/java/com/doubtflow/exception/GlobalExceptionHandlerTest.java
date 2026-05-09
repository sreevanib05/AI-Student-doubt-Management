package com.doubtflow.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.sql.SQLException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void returnsServiceUnavailableForDatabaseFailures() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        RuntimeException exception = new RuntimeException("Could not save student.", new SQLException("Database down."));

        var response = handler.handleUnexpected(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody())
                .containsEntry(
                        "message",
                        "Database is unavailable. Check the deployed DB_URL host, port, database name, username, and password."
                );
    }

    @Test
    void keepsUnexpectedFailuresGeneric() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        var response = handler.handleUnexpected(new RuntimeException("Boom."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(Map.of("message", "Something went wrong: Boom."));
    }
}
