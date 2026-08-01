package com.studentsupport.repository;

import com.studentsupport.entity.CvFeedback;
import com.studentsupport.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CvFeedbackRepository extends JpaRepository<CvFeedback, Long> {

    List<CvFeedback> findByUserOrderByCreatedAtDesc(User user);

    Optional<CvFeedback> findByCvFeedbackIdAndUser(Long cvFeedbackId, User user);

    long countByUser(User user);
}
