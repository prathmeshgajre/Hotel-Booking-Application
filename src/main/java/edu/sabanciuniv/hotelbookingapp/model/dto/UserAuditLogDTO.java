package edu.sabanciuniv.hotelbookingapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuditLogDTO {

    private String fieldName;
    private String originalValue;   // BLACK - registration data
    private String currentValue;    // RED   - current data in DB
    private String changedValue;    // GREEN - latest change (null if no change)
    private LocalDateTime changedAt;
    private boolean hasChanged;     // true if field was ever updated

}
