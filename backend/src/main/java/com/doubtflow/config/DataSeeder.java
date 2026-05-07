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
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setName("Faculty Admin");
            admin.setEmail("admin@doubtflow.ai");
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            adminRepository.save(admin);
        }

        if (mentorRepository.count() == 0) {
            createMentor("Aarav Mentor", "mentor.concepts@doubtflow.ai", "Conceptual Java");
            createMentor("Meera Mentor", "mentor.coding@doubtflow.ai", "Coding Practice");
            createMentor("Rahul Mentor", "mentor.debugging@doubtflow.ai", "Debugging Errors");
        }
    }

    private void createMentor(String name, String email, String expertise) {
        Mentor mentor = new Mentor();
        mentor.setName(name);
        mentor.setEmail(email);
        mentor.setPasswordHash(passwordEncoder.encode("Mentor@123"));
        mentor.setExpertise(expertise);
        mentor.setActive(true);
        mentorRepository.save(mentor);
    }
}
