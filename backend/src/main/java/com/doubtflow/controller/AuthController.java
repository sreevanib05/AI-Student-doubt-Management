package com.doubtflow.controller;

import com.doubtflow.dto.AuthResponse;
import com.doubtflow.dto.CreateMentorRequest;
import com.doubtflow.dto.LoginRequest;
import com.doubtflow.dto.RegisterRequest;
import com.doubtflow.model.UserPrincipal;
import com.doubtflow.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"/auth/student/register", "/students/register"})
    public AuthResponse registerStudent(@RequestBody RegisterRequest request) {
        return authService.registerStudent(request);
    }

    @PostMapping("/auth/mentor/register")
    public AuthResponse registerMentor(@RequestBody CreateMentorRequest request) {
        return authService.registerMentor(request);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/students/login")
    public AuthResponse studentLogin(@RequestBody LoginRequest request) {
        return authService.loginStudentOnly(request);
    }

    @GetMapping("/auth/me")
    public Map<String, Object> currentUser(Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();

        return Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        );
    }
}
