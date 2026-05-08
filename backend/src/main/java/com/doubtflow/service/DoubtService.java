package com.doubtflow.service;

import com.doubtflow.dto.AssignMentorRequest;
import com.doubtflow.dto.CreateDoubtRequest;
import com.doubtflow.dto.DoubtAttachment;
import com.doubtflow.dto.DoubtStatusRequest;
import com.doubtflow.dto.FAQSuggestion;
import com.doubtflow.dto.MentorResponseRequest;
import com.doubtflow.exception.DuplicateDoubtException;
import com.doubtflow.exception.InvalidCategoryException;
import com.doubtflow.model.Doubt;
import com.doubtflow.model.DoubtCategory;
import com.doubtflow.model.DoubtFactory;
import com.doubtflow.model.DoubtStatus;
import com.doubtflow.model.Mentor;
import com.doubtflow.model.Response;
import com.doubtflow.model.Role;
import com.doubtflow.model.UserPrincipal;
import com.doubtflow.repository.DoubtRepository;
import com.doubtflow.repository.MentorRepository;
import com.doubtflow.repository.ResponseRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
public class DoubtService {

    private static final int MAX_SUBJECT_LENGTH = 80;
    private static final int MAX_CONTEXT_LENGTH = 3000;
    private static final int MAX_PROMPT_LENGTH = 2500;
    private static final int MAX_PDF_BYTES = 3 * 1024 * 1024;

    private final DoubtRepository doubtRepository;
    private final MentorRepository mentorRepository;
    private final ResponseRepository responseRepository;
    private final FAQSuggestionService faqSuggestionService;

    public DoubtService(
            DoubtRepository doubtRepository,
            MentorRepository mentorRepository,
            ResponseRepository responseRepository,
            FAQSuggestionService faqSuggestionService
    ) {
        this.doubtRepository = doubtRepository;
        this.mentorRepository = mentorRepository;
        this.responseRepository = responseRepository;
        this.faqSuggestionService = faqSuggestionService;
    }

    public Doubt createDoubt(CreateDoubtRequest request, UserPrincipal currentUser)
            throws InvalidCategoryException, DuplicateDoubtException {

        requireRole(currentUser, Role.STUDENT);
        validateDoubt(request);

        DoubtCategory category = DoubtCategory.from(request.category());

        if (doubtRepository.existsDuplicateForStudent(currentUser.getId(), request.title(), request.description())) {
            throw new DuplicateDoubtException("You already have a similar unresolved doubt.");
        }

        // This line demonstrates inheritance: we create a child class based on the category.
        Doubt doubt = DoubtFactory.createDoubt(category.name());
        doubt.setTitle(request.title().trim());
        doubt.setDescription(request.description().trim());
        doubt.setSubject(normalizeSubject(request.subject()));
        doubt.setContextNotes(normalizeOptionalText(request.contextNotes(), MAX_CONTEXT_LENGTH, "Context notes"));
        doubt.setPromptTemplate(normalizeOptionalText(request.promptTemplate(), MAX_PROMPT_LENGTH, "Prompt template"));
        attachPdfIfPresent(doubt, request);
        doubt.setStudentId(currentUser.getId());

        // Admin/faculty assigns the mentor from the Admin dashboard.
        doubt.setStatus(DoubtStatus.OPEN);

        return doubtRepository.save(doubt);
    }

    public DoubtAttachment getAttachment(Long doubtId, UserPrincipal currentUser) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new IllegalArgumentException("Doubt not found."));

        if (!canViewDoubt(doubt, currentUser)) {
            throw new AccessDeniedException("You cannot view this doubt attachment.");
        }

        return doubtRepository.findAttachmentById(doubtId)
                .orElseThrow(() -> new IllegalArgumentException("No PDF attachment found for this doubt."));
    }

    public Doubt assignMentor(Long doubtId, AssignMentorRequest request, UserPrincipal currentUser) {
        requireRole(currentUser, Role.ADMIN);

        if (request.mentorId() == null) {
            throw new IllegalArgumentException("Mentor id is required.");
        }

        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new IllegalArgumentException("Doubt not found."));

        if (doubt.getStatus() == DoubtStatus.RESOLVED) {
            throw new IllegalArgumentException("Resolved doubts cannot be reassigned.");
        }

        Mentor mentor = mentorRepository.findById(request.mentorId())
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found."));

        if (!mentor.isActive()) {
            throw new IllegalArgumentException("Selected mentor is inactive.");
        }

        doubt.assignMentor(mentor);
        doubtRepository.assignMentor(doubtId, mentor.getId());

        return doubtRepository.findById(doubtId).orElseThrow();
    }

    public Doubt unassignMentor(Long doubtId, UserPrincipal currentUser) {
        requireRole(currentUser, Role.ADMIN);

        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new IllegalArgumentException("Doubt not found."));

        if (doubt.getStatus() == DoubtStatus.RESOLVED) {
            throw new IllegalArgumentException("Resolved doubts cannot be unassigned.");
        }

        doubtRepository.unassignMentor(doubtId);
        return doubtRepository.findById(doubtId).orElseThrow();
    }

    public List<FAQSuggestion> suggestFaqs(String text, UserPrincipal currentUser) {
        requireRole(currentUser, Role.STUDENT);
        return faqSuggestionService.suggest(text);
    }

    public List<Doubt> getMyDoubts(UserPrincipal currentUser) {
        requireRole(currentUser, Role.STUDENT);
        return doubtRepository.findByStudentId(currentUser.getId());
    }

    public List<Doubt> getAssignedDoubts(UserPrincipal currentUser) {
        requireRole(currentUser, Role.MENTOR);
        return doubtRepository.findAssignedToMentor(currentUser.getId());
    }

    public List<Doubt> getAllDoubts(UserPrincipal currentUser) {
        requireRole(currentUser, Role.ADMIN);
        return doubtRepository.findAll();
    }

    public List<Doubt> getDoubtsByCategory(String categoryValue, UserPrincipal currentUser) throws InvalidCategoryException {
        requireRole(currentUser, Role.ADMIN);
        DoubtCategory category = DoubtCategory.from(categoryValue);
        return doubtRepository.findByCategory(category);
    }

    public Doubt updateStatus(Long doubtId, DoubtStatusRequest request, UserPrincipal currentUser) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new IllegalArgumentException("Doubt not found."));

        if (currentUser.getRole() == Role.MENTOR && !currentUser.getId().equals(doubt.getMentorId())) {
            throw new AccessDeniedException("Mentors can update only their assigned doubts.");
        }

        if (currentUser.getRole() != Role.MENTOR && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only mentors or admins can update doubt status.");
        }

        DoubtStatus status = DoubtStatus.from(request.status());
        doubtRepository.updateStatus(doubtId, status);

        return doubtRepository.findById(doubtId).orElseThrow();
    }

    public Response respondToDoubt(MentorResponseRequest request, UserPrincipal currentUser) {
        requireRole(currentUser, Role.MENTOR);

        if (request.doubtId() == null) {
            throw new IllegalArgumentException("Doubt id is required.");
        }

        if (request.responseText() == null || request.responseText().isBlank()) {
            throw new IllegalArgumentException("Response text is required.");
        }

        Doubt doubt = doubtRepository.findById(request.doubtId())
                .orElseThrow(() -> new IllegalArgumentException("Doubt not found."));

        if (!currentUser.getId().equals(doubt.getMentorId())) {
            throw new AccessDeniedException("This doubt is assigned to another mentor.");
        }

        Response response = new Response();
        response.setDoubtId(request.doubtId());
        response.setMentorId(currentUser.getId());
        response.setResponseText(request.responseText().trim());

        Response savedResponse = responseRepository.save(response);
        doubtRepository.updateStatus(request.doubtId(), DoubtStatus.RESOLVED);

        return savedResponse;
    }

    private void validateDoubt(CreateDoubtRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Doubt title is required.");
        }

        if (request.description() == null || request.description().isBlank()) {
            throw new IllegalArgumentException("Doubt description is required.");
        }
    }

    private String normalizeSubject(String subject) {
        String normalizedSubject = subject == null || subject.isBlank() ? "General" : subject.trim();

        if (normalizedSubject.length() > MAX_SUBJECT_LENGTH) {
            throw new IllegalArgumentException("Subject must be " + MAX_SUBJECT_LENGTH + " characters or less.");
        }

        return normalizedSubject;
    }

    private String normalizeOptionalText(String value, int maxLength, String label) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmedValue = value.trim();

        if (trimmedValue.length() > maxLength) {
            throw new IllegalArgumentException(label + " must be " + maxLength + " characters or less.");
        }

        return trimmedValue;
    }

    private void attachPdfIfPresent(Doubt doubt, CreateDoubtRequest request) {
        if (request.pdfData() == null || request.pdfData().isBlank()) {
            return;
        }

        String pdfData = stripDataUrlPrefix(request.pdfData().trim());
        byte[] pdfBytes;

        try {
            pdfBytes = Base64.getDecoder().decode(pdfData);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("PDF attachment is not valid.");
        }

        if (pdfBytes.length > MAX_PDF_BYTES) {
            throw new IllegalArgumentException("PDF attachment must be 3 MB or smaller.");
        }

        if (!startsWithPdfHeader(pdfBytes)) {
            throw new IllegalArgumentException("Only PDF attachments are allowed.");
        }

        doubt.setPdfData(Base64.getEncoder().encodeToString(pdfBytes));
        doubt.setPdfFileName(normalizePdfFileName(request.pdfFileName()));
        doubt.setPdfContentType("application/pdf");
        doubt.setHasPdfAttachment(true);
    }

    private String stripDataUrlPrefix(String pdfData) {
        int commaIndex = pdfData.indexOf(',');
        return pdfData.startsWith("data:") && commaIndex >= 0 ? pdfData.substring(commaIndex + 1) : pdfData;
    }

    private boolean startsWithPdfHeader(byte[] bytes) {
        return bytes.length >= 4
                && bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F';
    }

    private String normalizePdfFileName(String fileName) {
        String normalizedFileName = fileName == null || fileName.isBlank() ? "doubt-attachment.pdf" : fileName.trim();
        normalizedFileName = normalizedFileName.replace("\\", "/");
        int slashIndex = normalizedFileName.lastIndexOf('/');

        if (slashIndex >= 0) {
            normalizedFileName = normalizedFileName.substring(slashIndex + 1);
        }

        normalizedFileName = normalizedFileName.replaceAll("[\\r\\n\"]", "").trim();

        if (!normalizedFileName.toLowerCase().endsWith(".pdf")) {
            normalizedFileName = normalizedFileName + ".pdf";
        }

        return normalizedFileName.length() > 120 ? normalizedFileName.substring(0, 116) + ".pdf" : normalizedFileName;
    }

    private boolean canViewDoubt(Doubt doubt, UserPrincipal currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return true;
        }

        if (currentUser.getRole() == Role.STUDENT) {
            return currentUser.getId().equals(doubt.getStudentId());
        }

        return currentUser.getRole() == Role.MENTOR && currentUser.getId().equals(doubt.getMentorId());
    }

    private void requireRole(UserPrincipal user, Role expectedRole) {
        if (user.getRole() != expectedRole) {
            throw new AccessDeniedException("This action requires " + expectedRole.name() + " access.");
        }
    }
}
