package com.doubtflow.service;

import com.doubtflow.dto.AuthResponse;
import com.doubtflow.dto.CreateMentorRequest;
import com.doubtflow.dto.LoginRequest;
import com.doubtflow.dto.RegisterRequest;
import com.doubtflow.model.Admin;
import com.doubtflow.model.Mentor;
import com.doubtflow.model.Role;
import com.doubtflow.model.Student;
import com.doubtflow.model.UserPrincipal;
import com.doubtflow.repository.AdminRepository;
import com.doubtflow.repository.MentorRepository;
import com.doubtflow.repository.StudentRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            StudentRepository studentRepository,
            MentorRepository mentorRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.studentRepository = studentRepository;
        this.mentorRepository = mentorRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse registerStudent(RegisterRequest request) {
        validateRegistration(request);
        String email = normalizeEmail(request.email());

        if (studentRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("A student with this email already exists.");
        }

        Student student = new Student();
        student.setName(request.name().trim());
        student.setEmail(email);
        student.setPasswordHash(passwordEncoder.encode(request.password()));

        Student savedStudent = studentRepository.save(student);
        UserPrincipal principal = new UserPrincipal(savedStudent.getId(), savedStudent.getName(), savedStudent.getEmail(), Role.STUDENT);

        return toAuthResponse(principal);
    }

    public AuthResponse registerMentor(CreateMentorRequest request) {
        validateMentorRegistration(request);
        String email = normalizeEmail(request.email());

        if (mentorRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("A mentor with this email already exists.");
        }

        Mentor mentor = new Mentor();
        mentor.setName(request.name().trim());
        mentor.setEmail(email);
        mentor.setPasswordHash(passwordEncoder.encode(request.password()));
        mentor.setExpertise(request.expertise().trim());
        mentor.setActive(true);

        Mentor savedMentor = mentorRepository.save(mentor);
        UserPrincipal principal = new UserPrincipal(savedMentor.getId(), savedMentor.getName(), savedMentor.getEmail(), Role.MENTOR);

        return toAuthResponse(principal);
    }

    public AuthResponse login(LoginRequest request) {
        Role role = Role.from(request.role());
        String email = normalizeEmail(request.email());

        if (request.password() == null || request.password().isBlank()) {
            throw new BadCredentialsException("Password is required.");
        }

        return switch (role) {
            case STUDENT -> loginStudent(email, request.password());
            case MENTOR -> loginMentor(email, request.password());
            case ADMIN -> loginAdmin(email, request.password());
        };
    }

    public AuthResponse loginStudentOnly(LoginRequest request) {
        return login(new LoginRequest(request.email(), request.password(), Role.STUDENT.name()));
    }

    private AuthResponse loginStudent(String email, String password) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid student login."));

        if (!passwordEncoder.matches(password, student.getPasswordHash())) {
            throw new BadCredentialsException("Invalid student login.");
        }

        return toAuthResponse(new UserPrincipal(student.getId(), student.getName(), student.getEmail(), Role.STUDENT));
    }

    private AuthResponse loginMentor(String email, String password) {
        Mentor mentor = mentorRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid mentor login."));

        if (!passwordEncoder.matches(password, mentor.getPasswordHash())) {
            throw new BadCredentialsException("Invalid mentor login.");
        }

        return toAuthResponse(new UserPrincipal(mentor.getId(), mentor.getName(), mentor.getEmail(), Role.MENTOR));
    }

    private AuthResponse loginAdmin(String email, String password) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid admin login."));

        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new BadCredentialsException("Invalid admin login.");
        }

        return toAuthResponse(new UserPrincipal(admin.getId(), admin.getName(), admin.getEmail(), Role.ADMIN));
    }

    private AuthResponse toAuthResponse(UserPrincipal principal) {
        String token = jwtService.generateToken(principal);
        return new AuthResponse(token, principal.getRole().name(), principal.getId(), principal.getName(), principal.getEmail());
    }

    private void validateRegistration(RegisterRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (request.password() == null || request.password().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
    }

    private void validateMentorRegistration(CreateMentorRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (request.password() == null || request.password().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }

        if (request.expertise() == null || request.expertise().isBlank()) {
            throw new IllegalArgumentException("Mentor expertise is required.");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        return email.trim().toLowerCase();
    }
}
