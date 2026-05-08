package com.doubtflow.controller;

import com.doubtflow.dto.CreateDoubtRequest;
import com.doubtflow.dto.DoubtAttachment;
import com.doubtflow.dto.DoubtStatusRequest;
import com.doubtflow.dto.FAQSuggestion;
import com.doubtflow.exception.DuplicateDoubtException;
import com.doubtflow.exception.InvalidCategoryException;
import com.doubtflow.model.Doubt;
import com.doubtflow.model.UserPrincipal;
import com.doubtflow.service.DoubtService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doubts")
public class DoubtController {

    private final DoubtService doubtService;

    public DoubtController(DoubtService doubtService) {
        this.doubtService = doubtService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('STUDENT')")
    public Doubt createDoubt(@RequestBody CreateDoubtRequest request, Authentication authentication)
            throws InvalidCategoryException, DuplicateDoubtException {

        return doubtService.createDoubt(request, currentUser(authentication));
    }

    @PostMapping("/suggestions")
    @PreAuthorize("hasRole('STUDENT')")
    public List<FAQSuggestion> suggestFaqs(@RequestBody Map<String, String> body, Authentication authentication) {
        return doubtService.suggestFaqs(body.get("text"), currentUser(authentication));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public List<Doubt> myDoubts(Authentication authentication) {
        return doubtService.getMyDoubts(currentUser(authentication));
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasRole('MENTOR')")
    public List<Doubt> assignedDoubts(Authentication authentication) {
        return doubtService.getAssignedDoubts(currentUser(authentication));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Doubt> allDoubts(Authentication authentication) {
        return doubtService.getAllDoubts(currentUser(authentication));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Doubt> byCategory(@PathVariable String category, Authentication authentication) throws InvalidCategoryException {
        return doubtService.getDoubtsByCategory(category, currentUser(authentication));
    }

    @GetMapping("/{id}/attachment")
    @PreAuthorize("hasAnyRole('STUDENT', 'MENTOR', 'ADMIN')")
    public ResponseEntity<byte[]> attachment(@PathVariable Long id, Authentication authentication) {
        DoubtAttachment attachment = doubtService.getAttachment(id, currentUser(authentication));
        MediaType contentType = attachment.contentType() == null
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType(attachment.contentType());

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(attachment.fileName())
                        .build()
                        .toString())
                .body(attachment.data());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public Doubt updateStatus(@PathVariable Long id, @RequestBody DoubtStatusRequest request, Authentication authentication) {
        return doubtService.updateStatus(id, request, currentUser(authentication));
    }

    private UserPrincipal currentUser(Authentication authentication) {
        return (UserPrincipal) authentication.getPrincipal();
    }
}
