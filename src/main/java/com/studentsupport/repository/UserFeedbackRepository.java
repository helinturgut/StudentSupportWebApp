package com.studentsupport.repository;

import com.studentsupport.entity.User;
import com.studentsupport.entity.UserFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {

    List<UserFeedback> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT AVG(f.rating) FROM UserFeedback f")
    Double findAverageRating();

    long count();
}
