package com.doubtflow.controller;

import com.doubtflow.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final DataSource dataSource;
    private final JwtService jwtService;
    private final String datasourceUrl;
    private final String activeProfile;

    public HealthController(
            DataSource dataSource,
            JwtService jwtService,
            @Value("${spring.datasource.url:}") String datasourceUrl,
            @Value("${spring.profiles.active:}") String activeProfile
    ) {
        this.dataSource = dataSource;
        this.jwtService = jwtService;
        this.datasourceUrl = datasourceUrl;
        this.activeProfile = activeProfile;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        boolean jwtReady = jwtService.isConfigured();

        Map<String, String> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("databaseConfig", datasourceUrl == null || datasourceUrl.isBlank() ? "MISSING" : "SET");
        body.put("jwt", jwtReady ? "UP" : "DOWN");
        body.put("profile", activeProfile == null || activeProfile.isBlank() ? "default" : activeProfile);

        return ResponseEntity.ok(body);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        boolean databaseReady = isDatabaseReady();
        boolean jwtReady = jwtService.isConfigured();
        boolean ready = databaseReady && jwtReady;

        Map<String, String> body = new LinkedHashMap<>();
        body.put("status", ready ? "UP" : "DOWN");
        body.put("database", databaseReady ? "UP" : "DOWN");
        body.put("databaseConfig", datasourceUrl == null || datasourceUrl.isBlank() ? "MISSING" : "SET");
        body.put("jwt", jwtReady ? "UP" : "DOWN");
        body.put("profile", activeProfile == null || activeProfile.isBlank() ? "default" : activeProfile);

        return ResponseEntity.status(ready ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    private boolean isDatabaseReady() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception exception) {
            return false;
        }
    }
}
