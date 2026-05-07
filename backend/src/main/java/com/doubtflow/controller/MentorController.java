package com.doubtflow.controller;

import com.doubtflow.dto.MentorResponseRequest;
import com.doubtflow.model.Doubt;
import com.doubtflow.model.Mentor;
import com.doubtflow.model.Response;
import com.doubtflow.model.UserPrincipal;
import com.doubtflow.repository.MentorRepository;
import com.doubtflow.service.DoubtService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mentors")
public class MentorController {

    private final MentorRepository mentorRepository;
    private final DoubtService doubtService;

    public MentorController(MentorRepository mentorRepository, DoubtService doubtService) {
        this.mentorRepository = mentorRepository;
        this.doubtService = doubtService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Mentor> allMentors() {
        return mentorRepository.findAll();
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('MENTOR')")
    public List<Doubt> dashboard(Authentication authentication) {
        return doubtService.getAssignedDoubts(currentUser(authentication));
    }

    @PostMapping("/respond")
    @PreAuthorize("hasRole('MENTOR')")
    public Response respond(@RequestBody MentorResponseRequest request, Authentication authentication) {
        return doubtService.respondToDoubt(request, currentUser(authentication));
    }

    private UserPrincipal currentUser(Authentication authentication) {
        return (UserPrincipal) authentication.getPrincipal();
    }
}
