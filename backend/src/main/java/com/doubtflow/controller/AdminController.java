package com.doubtflow.controller;

import com.doubtflow.dto.AnalyticsResponse;
import com.doubtflow.dto.AssignMentorRequest;
import com.doubtflow.dto.CreateMentorRequest;
import com.doubtflow.dto.SimulationResult;
import com.doubtflow.model.Doubt;
import com.doubtflow.model.Mentor;
import com.doubtflow.model.UserPrincipal;
import com.doubtflow.service.AdminService;
import com.doubtflow.service.DoubtService;
import com.doubtflow.service.MentorSimulationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final DoubtService doubtService;
    private final MentorSimulationService mentorSimulationService;

    public AdminController(AdminService adminService, DoubtService doubtService, MentorSimulationService mentorSimulationService) {
        this.adminService = adminService;
        this.doubtService = doubtService;
        this.mentorSimulationService = mentorSimulationService;
    }

    @GetMapping("/analytics")
    public AnalyticsResponse analytics() {
        return adminService.getAnalytics();
    }

    @PostMapping("/mentors")
    public Mentor createMentor(@RequestBody CreateMentorRequest request) {
        return adminService.createMentor(request);
    }

    @PatchMapping("/doubts/{doubtId}/assign")
    public Doubt assignMentor(
            @PathVariable Long doubtId,
            @RequestBody AssignMentorRequest request,
            Authentication authentication
    ) {
        return doubtService.assignMentor(doubtId, request, currentUser(authentication));
    }

    @PatchMapping("/doubts/{doubtId}/unassign")
    public Doubt unassignMentor(@PathVariable Long doubtId, Authentication authentication) {
        return doubtService.unassignMentor(doubtId, currentUser(authentication));
    }

    @PostMapping("/simulate-mentors")
    public SimulationResult simulateMentors() {
        return mentorSimulationService.simulateMentorsWorkingTogether();
    }

    private UserPrincipal currentUser(Authentication authentication) {
        return (UserPrincipal) authentication.getPrincipal();
    }
}
