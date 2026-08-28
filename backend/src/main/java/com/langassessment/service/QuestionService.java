package com.langassessment.service;

import com.langassessment.dto.QuestionDTO;
import com.langassessment.entity.Language;
import com.langassessment.entity.Question;
import com.langassessment.repository.LanguageRepository;
import com.langassessment.repository.QuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final LanguageRepository languageRepository;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Question createQuestion(QuestionDTO dto) {
        Language language = languageRepository.findById(dto.getLanguageId())
                .orElseThrow(() -> new RuntimeException("Language not found"));

        Question question = Question.builder()
                .language(language)
                .moduleType(Question.ModuleType.valueOf(dto.getModuleType()))
                .cefrLevel(dto.getCefrLevel())
                .questionText(dto.getQuestionText())
                .questionNumber(dto.getQuestionNumber())
                .status(Question.QuestionStatus.valueOf(dto.getStatus() != null ? dto.getStatus() : "ACTIVE"))
                .build();

        return questionRepository.save(question);
    }

    @Transactional(readOnly = true)
    public Page<QuestionDTO> getQuestionsByLanguageAndModule(
            Integer languageId,
            String moduleType,
            Pageable pageable) {
        Language language = languageRepository.findById(languageId)
                .orElseThrow(() -> new RuntimeException("Language not found"));

        Page<Question> questions = questionRepository.findByLanguageAndModuleTypeAndStatus(
                language,
                Question.ModuleType.valueOf(moduleType),
                Question.QuestionStatus.ACTIVE,
                pageable
        );

        return questions.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestionsByCefrLevel(
            Integer languageId,
            String moduleType,
            String cefrLevel) {
        Language language = languageRepository.findById(languageId)
                .orElseThrow(() -> new RuntimeException("Language not found"));

        List<Question> questions = questionRepository.findByLanguageAndModuleTypeAndCefrLevelAndStatus(
                language,
                Question.ModuleType.valueOf(moduleType),
                cefrLevel,
                Question.QuestionStatus.ACTIVE
        );

        return questions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Question> getQuestionsByModuleAndLanguage(
            Integer languageId,
            String moduleType,
            Integer limit) {
        Language language = languageRepository.findById(languageId)
                .orElseThrow(() -> new RuntimeException("Language not found"));

        List<Question> questions = questionRepository.findByLanguageAndModuleTypeAndStatus(
                language,
                Question.ModuleType.valueOf(moduleType),
                Question.QuestionStatus.ACTIVE
        );

        return questions.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Question> getActiveQuestionsByModuleAndLanguage(
            Integer languageId,
            String moduleType,
            Integer limit) {
        Language language = languageRepository.findById(languageId)
                .orElseThrow(() -> new RuntimeException("Language not found"));

        List<Question> questions = questionRepository.findByLanguageAndModuleTypeAndStatus(
                language,
                Question.ModuleType.valueOf(moduleType),
                Question.QuestionStatus.ACTIVE
        );

        return questions.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional
    public Question updateQuestion(Integer id, QuestionDTO dto) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (dto.getQuestionText() != null) {
            question.setQuestionText(dto.getQuestionText());
        }
        if (dto.getCefrLevel() != null) {
            question.setCefrLevel(dto.getCefrLevel());
        }
        if (dto.getStatus() != null) {
            question.setStatus(Question.QuestionStatus.valueOf(dto.getStatus()));
        }

        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Integer id) {
        questionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Question getQuestionById(Integer id) {
        return questionRepository.findById(id).orElse(null);
    }

    public Object fetchQuestionOptions(String questionOptionsUri) {
        try {
            if (questionOptionsUri == null || questionOptionsUri.isEmpty()) {
                return null;
            }

            // Extract object name from MinIO URL
            String objectName = extractObjectNameFromUrl(questionOptionsUri);
            InputStream inputStream = storageService.downloadFile(objectName, "assessment");

            // Read JSON content
            return objectMapper.readValue(inputStream, Object.class);
        } catch (Exception e) {
            log.error("Failed to fetch question options: {}", e.getMessage());
            return null;
        }
    }

    public byte[] fetchAudioContent(String audioUrl) {
        try {
            if (audioUrl == null || audioUrl.isEmpty()) {
                return new byte[0];
            }

            // Extract object name from MinIO URL
            String objectName = extractObjectNameFromUrl(audioUrl);
            InputStream inputStream = storageService.downloadFile(objectName, "assessment");

            // Read bytes
            return inputStream.readAllBytes();
        } catch (Exception e) {
            log.error("Failed to fetch audio content: {}", e.getMessage());
            return new byte[0];
        }
    }

    private String extractObjectNameFromUrl(String url) {
        // URL format: http://minio:9000/bucket/path/to/object
        // or: minio:9000/bucket/path/to/object
        if (url == null || url.isEmpty()) {
            return url;
        }

        // Remove protocol if present
        String urlWithoutProtocol = url.replaceAll("^https?://", "");

        // Remove host and port
        if (urlWithoutProtocol.contains("/")) {
            int slashIndex = urlWithoutProtocol.indexOf("/");
            String afterHost = urlWithoutProtocol.substring(slashIndex + 1);

            // Remove bucket name (first part before next slash)
            if (afterHost.contains("/")) {
                int nextSlash = afterHost.indexOf("/");
                return afterHost.substring(nextSlash + 1);
            }
        }
        return url;
    }

    private QuestionDTO convertToDTO(Question question) {
        return QuestionDTO.builder()
                .id(question.getId())
                .languageId(question.getLanguage().getId())
                .languageName(question.getLanguage().getName())
                .moduleType(question.getModuleType().toString())
                .cefrLevel(question.getCefrLevel())
                .questionText(question.getQuestionText())
                .questionNumber(question.getQuestionNumber())
                .status(question.getStatus().toString())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }
}
