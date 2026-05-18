package com.jobtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_applications",
        indexes = {
                @Index(name = "idx_job_apps_user_id", columnList = "user_id"),
                @Index(name = "idx_job_apps_status", columnList = "status"),
                @Index(name = "idx_job_apps_created_at", columnList = "created_at")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_job_apps_user"))
    private User user;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "role", nullable = false, length = 150)
    private String role;

    @Column(name = "status", nullable = false, length = 100)
    @Builder.Default
    private String status = "Applied";

    @Column(name = "job_url", length = 500)
    private String jobUrl;

    @Column(name = "ctc_package", precision = 15, scale = 2)
    private BigDecimal ctcPackage;

    @Column(name = "date_applied")
    private LocalDate dateApplied;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
