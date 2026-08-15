package com.studentsupport.repository;

import com.studentsupport.entity.InterviewQuestion;
import com.studentsupport.entity.InterviewQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewQuestionOptionRepository extends JpaRepository<InterviewQuestionOption, Long> {

    List<InterviewQuestionOption> findByQuestionOrderByOptionIdAsc(InterviewQuestion question);

    void deleteByQuestion(InterviewQuestion question);
}
