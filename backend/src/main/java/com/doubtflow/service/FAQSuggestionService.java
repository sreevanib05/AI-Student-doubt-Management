package com.doubtflow.service;

import com.doubtflow.dto.FAQSuggestion;
import com.doubtflow.model.SolvedDoubtAnswer;
import com.doubtflow.repository.DoubtRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FAQSuggestionService {

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "is", "am", "are", "to", "in", "of", "for", "and", "or", "a", "an", "my", "i", "how", "why"
    );

    private final DoubtRepository doubtRepository;

    public FAQSuggestionService(DoubtRepository doubtRepository) {
        this.doubtRepository = doubtRepository;
    }

    public List<FAQSuggestion> suggest(String newDoubtText) {
        if (newDoubtText == null || newDoubtText.isBlank()) {
            return List.of();
        }

        return doubtRepository.findSolvedDoubtsWithResponses()
                .stream()
                .filter(answer -> isSimilar(answer.getTitle() + " " + answer.getDescription(), newDoubtText))
                .limit(5)
                .map(this::toSuggestion)
                .toList();
    }

    public boolean isSimilar(String oldDoubt, String newDoubt) {
        String oldText = normalize(oldDoubt);
        String newText = normalize(newDoubt);

        if (oldText.contains(newText) || newText.contains(oldText)) {
            return true;
        }

        Set<String> oldKeywords = keywords(oldText);
        Set<String> newKeywords = keywords(newText);

        long commonKeywordCount = newKeywords.stream()
                .filter(oldKeywords::contains)
                .count();

        return commonKeywordCount >= 2;
    }

    private FAQSuggestion toSuggestion(SolvedDoubtAnswer answer) {
        return new FAQSuggestion(answer.getDoubtId(), answer.getTitle(), answer.getCategory(), answer.getResponseText());
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }

    private Set<String> keywords(String text) {
        return Arrays.stream(text.split("[^a-z0-9]+"))
                .filter(word -> word.length() > 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.toSet());
    }
}
