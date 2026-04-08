package com.pch.mng.course;

import com.pch.mng.global.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.status = :status " +
           "AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Course> findByStatusAndKeyword(@Param("status") CourseStatus status,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);
}
