package com.langassessment.repository;

import com.langassessment.entity.Assessment;
import com.langassessment.entity.AssessmentModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentModuleRepository extends JpaRepository<AssessmentModule, Integer> {
    List<AssessmentModule> findByAssessment(Assessment assessment);

    AssessmentModule findByAssessmentAndModuleType(Assessment assessment, AssessmentModule.ModuleType moduleType);
}
