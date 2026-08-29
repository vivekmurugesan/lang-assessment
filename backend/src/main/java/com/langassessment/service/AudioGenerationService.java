package com.langassessment.service;

import com.langassessment.entity.Language;
import com.langassessment.entity.Question;
import com.langassessment.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioGenerationService {

    private final QuestionRepository questionRepository;
    private final TextToSpeechService textToSpeechService;

    /**
     * Generate audio asynchronously for questions that need it
     */
    @Async
    @Transactional
    public void generateAudioForQuestions(List<Question> questions) {
        for (Question question : questions) {
            if (question.getModuleType() == Question.ModuleType.LISTENING
                && question.getAudioGenerationStatus() == Question.ContentGenerationStatus.PENDING) {
                generateAudioForQuestion(question);
            }
        }
    }

    /**
     * Generate audio for a single question
     */
    @Transactional
    public void generateAudioForQuestion(Question question) {
        if (question.getAudioGenerationStatus() != Question.ContentGenerationStatus.PENDING) {
            return;
        }

        try {
            question.setAudioGenerationStatus(Question.ContentGenerationStatus.GENERATING);
            questionRepository.save(question);

            if (!textToSpeechService.isAvailable()) {
                log.warn("Text-to-Speech service not available. Audio generation skipped for question {}", question.getId());
                question.setAudioGenerationStatus(Question.ContentGenerationStatus.PENDING);
                question.setAudioGenerationError("TTS service not configured. Please configure TTS API credentials.");
                questionRepository.save(question);
                return;
            }

            String languageCode = getLanguageCode(question.getLanguage());
            String audioUrl = textToSpeechService.synthesizeAndStore(
                question.getQuestionText(),
                languageCode,
                Optional.empty()
            );

            question.setAudioUrl(audioUrl);
            question.setAudioGenerationStatus(Question.ContentGenerationStatus.GENERATED);
            question.setAudioGenerationError(null);
            questionRepository.save(question);

            log.info("Successfully generated audio for question {}: {}", question.getId(), audioUrl);

        } catch (Exception e) {
            log.error("Failed to generate audio for question {}: {}", question.getId(), e.getMessage(), e);
            question.setAudioGenerationStatus(Question.ContentGenerationStatus.FAILED);
            question.setAudioGenerationError(e.getMessage());
            questionRepository.save(question);
        }
    }

    /**
     * Retry generating audio for failed questions
     */
    @Transactional
    public void retryFailedAudioGeneration() {
        List<Question> failedQuestions = questionRepository.findByAudioGenerationStatus(
            Question.ContentGenerationStatus.FAILED
        );

        for (Question question : failedQuestions) {
            log.info("Retrying audio generation for question {}", question.getId());
            question.setAudioGenerationStatus(Question.ContentGenerationStatus.PENDING);
            generateAudioForQuestion(question);
        }
    }

    private String getLanguageCode(Language language) {
        String langCode = language.getCode().toUpperCase();
        return switch (langCode) {
            case "EN" -> "en-US";
            case "ES" -> "es-ES";
            case "FR" -> "fr-FR";
            case "DE" -> "de-DE";
            case "PT" -> "pt-BR";
            case "IT" -> "it-IT";
            case "JP" -> "ja-JP";
            case "ZH" -> "zh-CN";
            default -> "en-US";
        };
    }
}
