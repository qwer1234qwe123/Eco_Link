package com.ecolink.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "collection_route")
@Getter
@NoArgsConstructor
public class CollectionRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Worker worker;

    @Column(name = "optimized_path", nullable = false, columnDefinition = "JSON")
    private String optimizedPath;

    @Column(name = "total_distance", nullable = false)
    private Double totalDistance;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "collectionRoute")
    private List<CollectionHistory> collectionHistories;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.totalDistance = 0.0;
    }
}