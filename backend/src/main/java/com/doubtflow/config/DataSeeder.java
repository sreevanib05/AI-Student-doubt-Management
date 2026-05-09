package com.doubtflow.config;

import com.doubtflow.model.Admin;
import com.doubtflow.model.Mentor;
import com.doubtflow.repository.AdminRepository;
import com.doubtflow.repository.MentorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);

    private final AdminRepository adminRepository;
    private final MentorRepository mentorRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AdminRepository adminRepository, MentorRepository mentorRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.mentorRepository = mentorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void seedStarterAccounts() {
        try {
            createAdminIfMissing("Faculty Admin", "admin@doubtflow.ai");
            createMentorIfMissing("Aarav Mentor", "mentor.concepts@doubtflow.ai", "Conceptual Java");
            createMentorIfMissing("Meera Mentor", "mentor.coding@doubtflow.ai", "Coding Practice");
            createMentorIfMissing("Rahul Mentor", "mentor.debugging@doubtflow.ai", "Debugging Errors");
        } catch (Exception exception) {
            LOGGER.warn("Starter account seeding could not complete. Check database connectivity and schema.", exception);
        }
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
