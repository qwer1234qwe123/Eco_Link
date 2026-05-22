
package com.ecolink.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "trash_can")
@Getter
@NoArgsConstructor
public class TrashCan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loc_name", nullable = false, length = 100)
    private String locName;

    @Column(name = "loc_lat", nullable = false)
    private Double locLat;

    @Column(name = "loc_lng", nullable = false)
    private Double locLng;

    @Column(name = "max_capa", nullable = false)
    private Integer maxCapa;

    @JsonIgnore
    @OneToMany(mappedBy = "trashCan")
    private List<SensorLog> sensorLogs;

    @JsonIgnore
    @OneToMany(mappedBy = "trashCan")
    private List<CanStatusLog> statusLogs;

    @JsonIgnore
    @OneToMany(mappedBy = "trashCan")
    private List<CollectionHistory> collectionHistories;

    public TrashCan(String locName, Double locLat, Double locLng, Integer maxCapa) {
        // 새 쓰레기통 생성할 때 값을 넣어주는 생성자
        this.locName = locName; // 위치 이름
        this.locLat = locLat; // 위도
        this.locLng = locLng; // 경도
        this.maxCapa = maxCapa; // 최대 용량
    }

    public void update(String locName, Double locLat, Double locLng, Integer maxCapa) {
        // 쓰레기통 정보 수정할 때 값을 업데이트하는 메서드
        this.locName = locName; // 위치 이름
        this.locLat = locLat; // 위도
        this.locLng = locLng; // 경도
        this.maxCapa = maxCapa; // 최대 용량
    }

}