package com.langassessment.service;

import com.langassessment.dto.QuestionDTO;
import com.langassessment.entity.Language;
import com.langassessment.entity.Question;
import com.langassessment.repository.LanguageRepository;
import com.langassessment.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final LanguageRepository languageRepository;

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
