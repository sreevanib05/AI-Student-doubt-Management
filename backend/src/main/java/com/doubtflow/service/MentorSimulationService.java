package com.doubtflow.service;

import com.doubtflow.dto.SimulationResult;
import com.doubtflow.model.Mentor;
import com.doubtflow.repository.MentorRepository;
import com.doubtflow.thread.MentorResolutionWorker;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class MentorSimulationService {

    private final MentorRepository mentorRepository;

    public MentorSimulationService(MentorRepository mentorRepository) {
        this.mentorRepository = mentorRepository;
    }

    public SimulationResult simulateMentorsWorkingTogether() {
        List<Mentor> mentors = mentorRepository.findAll()
                .stream()
                .filter(Mentor::isActive)
                .limit(3)
                .toList();

        if (mentors.isEmpty()) {
            return new SimulationResult(List.of("No active mentors are available for simulation."));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(mentors.size());
        List<Future<String>> futures = new ArrayList<>();

        for (Mentor mentor : mentors) {
            futures.add(executorService.submit(new MentorResolutionWorker(mentor)));
        }

        List<String> messages = new ArrayList<>();
        for (Future<String> future : futures) {
            try {
                messages.add(future.get());
            } catch (Exception exception) {
                messages.add("A mentor task failed: " + exception.getMessage());
            }
        }

        executorService.shutdown();
        return new SimulationResult(messages);
    }
}
