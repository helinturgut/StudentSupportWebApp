package com.studentsupport.repository;

import com.studentsupport.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    Page<Resource> findByCategoryContainingIgnoreCase(String category, Pageable pageable);

    @Query("SELECT r FROM Resource r WHERE " +
           "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Resource> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT r FROM Resource r WHERE " +
           "(:keyword IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:category IS NULL OR LOWER(r.category) LIKE LOWER(CONCAT('%', :category, '%')))")
    Page<Resource> findByKeywordAndCategory(@Param("keyword") String keyword,
                                            @Param("category") String category,
                                            Pageable pageable);

    List<Resource> findByCategoryContainingIgnoreCase(String category);
}
