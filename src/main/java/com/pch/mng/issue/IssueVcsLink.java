package com.pch.mng.issue;

import com.pch.mng.global.enums.VcsLinkKind;
import com.pch.mng.global.enums.VcsProvider;
import com.pch.mng.user.UserAccount;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "issue_vcs_link_tb")
public class IssueVcsLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VcsProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_kind", nullable = false, length = 32)
    private VcsLinkKind linkKind;

    @Column(nullable = false, length = 2000)
    private String url;

    @Column(length = 500)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserAccount createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public IssueVcsLink(
            Issue issue, VcsProvider provider, VcsLinkKind linkKind, String url, String title, UserAccount createdBy) {
        this.issue = issue;
        this.provider = provider;
        this.linkKind = linkKind;
        this.url = url;
        this.title = title;
        this.createdBy = createdBy;
    }
}
