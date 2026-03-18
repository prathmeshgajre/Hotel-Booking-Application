package edu.sabanciuniv.hotelbookingapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_audit_log")
public class UserAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which user this log belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    // Field that was changed
    @Column(nullable = false)
    private String fieldName;

    // Value before change (original at registration)
    @Column
    private String oldValue;

    // Value after change
    @Column
    private String newValue;

    // When this change happened
    @CreationTimestamp
    private LocalDateTime changedAt;

    // Type: ORIGINAL (registration data) or UPDATE (changed data)
    @Column(nullable = false)
    private String changeType; // "ORIGINAL" or "UPDATE"
}
