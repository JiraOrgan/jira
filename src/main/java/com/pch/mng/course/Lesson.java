package com.pch.mng.course;

import com.pch.mng.global.enums.ContentType;
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
@Table(name = "lesson_tb")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    private String videoUrl;

    private int orderIndex;

    private int durationMinutes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public Lesson(Section section, String title, String content, ContentType contentType,
                  String videoUrl, int orderIndex, int durationMinutes) {
        this.section = section;
        this.title = title;
        this.content = content;
        this.contentType = contentType != null ? contentType : ContentType.TEXT;
        this.videoUrl = videoUrl;
        this.orderIndex = orderIndex;
        this.durationMinutes = durationMinutes;
    }
}
