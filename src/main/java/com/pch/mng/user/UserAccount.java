package com.pch.mng.user;

import com.pch.mng.global.enums.UserRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "user_account_tb")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean isLocked;

    private int failedLoginAttempts;

    private LocalDateTime lockedUntil;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public UserAccount(String email, String password, String name, UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role != null ? role : UserRole.LEARNER;
        this.isLocked = false;
        this.failedLoginAttempts = 0;
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.isLocked = true;
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.isLocked = false;
        this.lockedUntil = null;
    }

    public boolean isAccountLocked() {
        if (!isLocked) return false;
        if (lockedUntil != null && LocalDateTime.now().isAfter(lockedUntil)) {
            resetFailedAttempts();
            return false;
        }
        return true;
    }
}
