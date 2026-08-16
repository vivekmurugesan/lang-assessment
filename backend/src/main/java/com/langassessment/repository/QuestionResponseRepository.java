package com.langassessment.repository;

import com.langassessment.entity.QuestionResponse;
import com.langassessment.entity.AssessmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionResponseRepository extends JpaRepository<QuestionResponse, Integer> {
    List<QuestionResponse> findBySubmission(AssessmentSubmission submission);
}
