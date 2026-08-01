package com.studentsupport.repository;

import com.studentsupport.entity.Recommendation;
import com.studentsupport.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findByUserOrderByCreatedAtDesc(User user);

    @Transactional
    void deleteByUser(User user);
}
