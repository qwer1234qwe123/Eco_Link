package com.ecolink.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "empty_history")
@Getter
@NoArgsConstructor
public class EmptyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore // ← Lazy 프록시 직렬화 방지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "can_id", nullable = false)
    private TrashCan trashCan;

    @JsonProperty("canId") // ← canId만 노출
    public Long getCanId() {
        return trashCan != null ? trashCan.getId() : null;
    }

    @Column(name = "before_level", nullable = false)
    private Float beforeLevel;

    @Column(name = "after_level", nullable = false)
    private Float afterLevel;

    @Column(name = "emptied_at", nullable = false)
    private LocalDateTime emptiedAt;

    @Column(name = "note")
    private String note;

    @Builder
    public EmptyHistory(TrashCan trashCan, Float beforeLevel,
            Float afterLevel, String note) {
        this.trashCan = trashCan;
        this.beforeLevel = beforeLevel;
        this.afterLevel = afterLevel;
        this.emptiedAt = LocalDateTime.now();
        this.note = note;
    }
}