package com.doubtflow.service;

import com.doubtflow.dto.AnalyticsResponse;
import com.doubtflow.dto.CreateMentorRequest;
import com.doubtflow.model.DoubtStatus;
import com.doubtflow.model.Mentor;
import com.doubtflow.repository.DoubtRepository;
import com.doubtflow.repository.MentorRepository;
import com.doubtflow.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final DoubtRepository doubtRepository;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            DoubtRepository doubtRepository,
            StudentRepository studentRepository,
            MentorRepository mentorRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.doubtRepository = doubtRepository;
        this.studentRepository = studentRepository;
        this.mentorRepository = mentorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AnalyticsResponse getAnalytics() {
        return new AnalyticsResponse(
                doubtRepository.countAll(),
                doubtRepository.countByStatus(DoubtStatus.OPEN),
                doubtRepository.countByStatus(DoubtStatus.ASSIGNED),
                doubtRepository.countByStatus(DoubtStatus.IN_PROGRESS),
                doubtRepository.countByStatus(DoubtStatus.RESOLVED),
                studentRepository.count(),
                mentorRepository.count(),
                doubtRepository.countByCategory(),
                mentorRepository.findWorkloads()
        );
    }

    public Mentor createMentor(CreateMentorRequest request) {
        validateMentor(request);
        String email = request.email().trim().toLowerCase();

        if (mentorRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("A mentor with this email already exists.");
        }

        Mentor mentor = new Mentor();
        mentor.setName(request.name().trim());
        mentor.setEmail(email);
        mentor.setPasswordHash(passwordEncoder.encode(request.password()));
        mentor.setExpertise(request.expertise().trim());
        mentor.setActive(true);

        return mentorRepository.save(mentor);
    }

    private void validateMentor(CreateMentorRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Mentor name is required.");
        }

        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Mentor email is required.");
        }

        if (request.password() == null || request.password().length() < 6) {
            throw new IllegalArgumentException("Mentor password must be at least 6 characters.");
        }

        if (request.expertise() == null || request.expertise().isBlank()) {
            throw new IllegalArgumentException("Mentor expertise is required.");
        }
    }
}
