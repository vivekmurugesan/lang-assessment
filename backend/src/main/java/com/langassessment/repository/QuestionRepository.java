package com.langassessment.repository;

import com.langassessment.entity.Language;
import com.langassessment.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Page<Question> findByLanguageAndModuleTypeAndStatus(
            Language language,
            Question.ModuleType moduleType,
            Question.QuestionStatus status,
            Pageable pageable
    );

    List<Question> findByLanguageAndModuleTypeAndCefrLevelAndStatus(
            Language language,
            Question.ModuleType moduleType,
            String cefrLevel,
            Question.QuestionStatus status
    );

    List<Question> findByLanguageAndStatus(Language language, Question.QuestionStatus status);
}
