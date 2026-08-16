package com.langassessment.repository;

import com.langassessment.entity.Assessment;
import com.langassessment.entity.AssessmentQuestionSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentQuestionSelectionRepository extends JpaRepository<AssessmentQuestionSelection, Integer> {
    List<AssessmentQuestionSelection> findByAssessmentOrderBySequenceNumber(Assessment assessment);
    void deleteByAssessment(Assessment assessment);
}
