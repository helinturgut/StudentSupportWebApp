package com.studentsupport.repository;

import com.studentsupport.entity.Difficulty;
import com.studentsupport.entity.InterviewQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    @Query("SELECT DISTINCT q.category FROM InterviewQuestion q ORDER BY q.category")
    List<String> findDistinctCategories();

    Page<InterviewQuestion> findByCategoryIgnoreCase(String category, Pageable pageable);

    Page<InterviewQuestion> findByDifficulty(Difficulty difficulty, Pageable pageable);

    Page<InterviewQuestion> findByCategoryIgnoreCaseAndDifficulty(String category, Difficulty difficulty, Pageable pageable);

    @Query("SELECT q FROM InterviewQuestion q WHERE " +
           "(:category IS NULL OR LOWER(q.category) = LOWER(CAST(:category AS string))) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty)")
    Page<InterviewQuestion> findByCategoryAndDifficulty(@Param("category") String category,
                                                         @Param("difficulty") Difficulty difficulty,
                                                         Pageable pageable);
}
