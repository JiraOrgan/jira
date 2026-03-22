package com.jira.mng.dashboard;

import com.jira.mng.user.UserAccount;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Data
@Entity
@Table(name = "dashboard_tb")
public class Dashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_shared", nullable = false)
    private boolean shared;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "dashboard", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DashboardGadget> gadgets;

    @Builder
    public Dashboard(UserAccount owner, String name, boolean shared) {
        this.owner = owner;
        this.name = name;
        this.shared = shared;
    }
}
