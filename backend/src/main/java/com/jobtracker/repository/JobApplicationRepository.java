package com.jobtracker.repository;

import com.jobtracker.entity.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    Page<JobApplication> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<JobApplication> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<JobApplication> findByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);

    long countByUserIdAndStatus(UUID userId, String status);

    @Query("SELECT ja.status, COUNT(ja) FROM JobApplication ja WHERE ja.user.id = :userId GROUP BY ja.status")
    List<Object[]> countByStatusForUser(@Param("userId") UUID userId);

    @Query(value = """
            SELECT TO_CHAR(created_at, 'YYYY-MM') AS month, COUNT(*) AS count
            FROM job_applications
            WHERE user_id = :userId
            AND created_at >= :since
            GROUP BY TO_CHAR(created_at, 'YYYY-MM')
            ORDER BY month ASC
            """, nativeQuery = true)
    List<Object[]> countMonthlyApplicationsForUser(
            @Param("userId") UUID userId,
            @Param("since") LocalDateTime since);

    @Query("SELECT ja.ctcPackage FROM JobApplication ja WHERE ja.user.id = :userId AND ja.ctcPackage IS NOT NULL")
    List<java.math.BigDecimal> findCtcPackagesByUserId(@Param("userId") UUID userId);
}
