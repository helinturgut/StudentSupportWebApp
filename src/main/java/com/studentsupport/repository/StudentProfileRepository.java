package com.studentsupport.repository;

import com.studentsupport.entity.StudentProfile;
import com.studentsupport.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUser(User user);

    Optional<StudentProfile> findByUserUserId(Long userId);
}
