package com.ecolink.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "collection_history")
@Getter
@NoArgsConstructor
public class CollectionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private CollectionRoute collectionRoute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "can_id", nullable = false)
    private TrashCan trashCan;

    @Column(name = "before_level", nullable = false)
    private Integer beforeLevel;

    @Column(name = "after_level", nullable = false)
    private Integer afterLevel;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;

    @PrePersist
    public void prePersist() {
        this.collectedAt = LocalDateTime.now();
    }
}
