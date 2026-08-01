package com.studentsupport.repository;

import com.studentsupport.entity.CareerPathway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CareerPathwayRepository extends JpaRepository<CareerPathway, Long> {

    @Query("SELECT cp FROM CareerPathway cp WHERE " +
           "LOWER(cp.title) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(cp.requiredSkills) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<CareerPathway> findByTitleOrSkillsContaining(@Param("term") String term);
}
