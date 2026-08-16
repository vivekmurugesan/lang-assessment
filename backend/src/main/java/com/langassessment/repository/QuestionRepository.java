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

    List<Question> findByLanguageAndModuleTypeAndStatus(
            Language language,
            Question.ModuleType moduleType,
            Question.QuestionStatus status
    );

    List<Question> findByStatusOrderByModuleTypeAsc(Question.QuestionStatus status);

    // Catalog-related queries
    List<Question> findByLanguageId(Integer languageId);

    Page<Question> findByLanguageIdAndApprovalStatus(
            Integer languageId,
            Question.ApprovalStatus approvalStatus,
            Pageable pageable
    );

    Page<Question> findByLanguageIdAndModuleTypeAndApprovalStatus(
            Integer languageId,
            Question.ModuleType moduleType,
            Question.ApprovalStatus approvalStatus,
            Pageable pageable
    );

    Page<Question> findByLanguageIdAndCatalogCategoryAndApprovalStatus(
            Integer languageId,
            String catalogCategory,
            Question.ApprovalStatus approvalStatus,
            Pageable pageable
    );

    Page<Question> findByLanguageIdAndModuleTypeAndApprovalStatusAndCatalogCategory(
            Integer languageId,
            Question.ModuleType moduleType,
            Question.ApprovalStatus approvalStatus,
            String catalogCategory,
            Pageable pageable
    );

    List<Question> findByLanguageIdAndModuleTypeAndApprovalStatus(
            Integer languageId,
            Question.ModuleType moduleType,
            Question.ApprovalStatus approvalStatus
    );
}
