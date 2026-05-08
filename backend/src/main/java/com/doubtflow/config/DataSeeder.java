package com.doubtflow.config;

import com.doubtflow.model.Admin;
import com.doubtflow.model.Mentor;
import com.doubtflow.repository.AdminRepository;
import com.doubtflow.repository.MentorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final MentorRepository mentorRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AdminRepository adminRepository, MentorRepository mentorRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.mentorRepository = mentorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createAdminIfMissing("Faculty Admin", "admin@doubtflow.ai");
        createMentorIfMissing("Aarav Mentor", "mentor.concepts@doubtflow.ai", "Conceptual Java");
        createMentorIfMissing("Meera Mentor", "mentor.coding@doubtflow.ai", "Coding Practice");
        createMentorIfMissing("Rahul Mentor", "mentor.debugging@doubtflow.ai", "Debugging Errors");
    }

    private void createAdminIfMissing(String name, String email) {
        if (adminRepository.findByEmail(email).isPresent()) {
            return;
        }

        Admin admin = new Admin();
        admin.setName(name);
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
        adminRepository.save(admin);
    }

    private void createMentorIfMissing(String name, String email, String expertise) {
        if (mentorRepository.findByEmail(email).isPresent()) {
            return;
        }

        Mentor mentor = new Mentor();
        mentor.setName(name);
        mentor.setEmail(email);
        mentor.setPasswordHash(passwordEncoder.encode("Mentor@123"));
        mentor.setExpertise(expertise);
        mentor.setActive(true);
        mentorRepository.save(mentor);
    }
}
