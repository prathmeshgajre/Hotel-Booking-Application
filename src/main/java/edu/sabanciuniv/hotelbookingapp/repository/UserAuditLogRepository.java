package edu.sabanciuniv.hotelbookingapp.repository;

import edu.sabanciuniv.hotelbookingapp.model.UserAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAuditLogRepository extends JpaRepository<UserAuditLog, Long> {

    // Get all logs for a user ordered by time
    List<UserAuditLog> findByUserIdOrderByChangedAtAsc(Long userId);

    /*// Get only original registration data
    List<UserAuditLog> findByUserIdAndChangeType(Long userId, String changeType);*/
}