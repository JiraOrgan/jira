package com.pch.mng.global.event;

import com.pch.mng.global.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "outbox_event_tb")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String payload;

    @Column(nullable = false)
    private String destinationTopic;

    @Column(unique = true, nullable = false)
    private String dedupKey;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private int retryCount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    @Builder
    public OutboxEvent(String eventType, String payload, String destinationTopic,
                       String dedupKey, OutboxStatus status) {
        this.eventType = eventType;
        this.payload = payload;
        this.destinationTopic = destinationTopic;
        this.dedupKey = dedupKey;
        this.status = status != null ? status : OutboxStatus.PENDING;
        this.retryCount = 0;
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void incrementRetry() {
        this.retryCount++;
    }

    public void markDeadLetter() {
        this.status = OutboxStatus.DEAD_LETTER;
    }
}
