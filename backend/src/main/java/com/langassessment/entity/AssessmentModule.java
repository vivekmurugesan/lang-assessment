package com.langassessment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Enumerated(EnumType.STRING)
    @Column(name = "module_type", nullable = false)
    private ModuleType moduleType;

    @Column(name = "num_questions", nullable = false)
    private Integer numQuestions;

    @Column(name = "difficulty_level")
    private String difficultyLevel;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ModuleType {
        LISTENING,
        READING,
        SPOKEN_INTERACTION,
        SPOKEN_PRODUCTION,
        WRITING
    }
}
