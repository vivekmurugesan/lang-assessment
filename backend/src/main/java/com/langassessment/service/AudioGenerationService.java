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
        log.info("🎵 [ASYNC] Audio generation started for {} questions", questions.size());

        List<Question> listeningQuestions = questions.stream()
            .filter(q -> q.getModuleType() == Question.ModuleType.LISTENING
                && q.getAudioGenerationStatus() == Question.ContentGenerationStatus.PENDING)
            .toList();

        log.info("🎵 [ASYNC] Found {} LISTENING questions requiring audio generation", listeningQuestions.size());

        if (listeningQuestions.isEmpty()) {
            log.info("🎵 [ASYNC] No LISTENING questions to process");
            return;
        }

        for (Question question : listeningQuestions) {
            log.info("🎵 [ASYNC] Processing audio generation for LISTENING question ID: {}", question.getId());
            generateAudioForQuestion(question);
        }

        log.info("🎵 [ASYNC] Audio generation batch completed");
    }

    /**
     * Generate audio for a single question
     */
    @Transactional
    public void generateAudioForQuestion(Question question) {
        log.info("🎵 Processing question ID {}, current status: {}", question.getId(), question.getAudioGenerationStatus());

        if (question.getAudioGenerationStatus() != Question.ContentGenerationStatus.PENDING) {
            log.debug("⏭️  Skipping question {} - status is {}, not PENDING",
                question.getId(), question.getAudioGenerationStatus());
            return;
        }

        try {
            log.info("🎵 Setting status to GENERATING for question {}", question.getId());
            question.setAudioGenerationStatus(Question.ContentGenerationStatus.GENERATING);
            questionRepository.save(question);

            log.info("🔍 Checking if TTS service is available...");
            if (!textToSpeechService.isAvailable()) {
                log.warn("❌ Text-to-Speech service NOT available for question {}", question.getId());
                question.setAudioGenerationStatus(Question.ContentGenerationStatus.PENDING);
                question.setAudioGenerationError("TTS service not available. Check EdgeTTS/Google TTS configuration.");
                questionRepository.save(question);
                return;
            }

            log.info("✅ TTS service is available. Proceeding with audio generation");
            String languageCode = getLanguageCode(question.getLanguage());
            log.info("🌐 Using language code: {}", languageCode);

            log.info("📝 Question text length: {} chars", question.getQuestionText().length());
            String audioUrl = textToSpeechService.synthesizeAndStore(
                question.getQuestionText(),
                languageCode,
                Optional.empty()
            );

            question.setAudioUrl(audioUrl);
            question.setAudioGenerationStatus(Question.ContentGenerationStatus.GENERATED);
            question.setAudioGenerationError(null);
            questionRepository.save(question);

            log.info("✅ Successfully generated audio for question {}: {}", question.getId(), audioUrl);

        } catch (Exception e) {
            log.error("❌ Failed to generate audio for question {}: {} | Cause: {}",
                question.getId(), e.getMessage(), e.getCause(), e);
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
