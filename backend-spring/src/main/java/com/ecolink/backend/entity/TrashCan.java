// package com.ecolink.backend.entity;

// import jakarta.persistence.*;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import java.util.List;

// @Entity
// @Table(name = "trash_can")
// @Getter
// @NoArgsConstructor
// public class TrashCan {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(name = "loc_name", nullable = false, length = 100)
//     private String locName;

//     @Column(name = "loc_lat", nullable = false)
//     private Double locLat;

//     @Column(name = "loc_lng", nullable = false)
//     private Double locLng;

//     @Column(name = "max_capa", nullable = false)
//     private Integer maxCapa;

//     @OneToMany(mappedBy = "trashCan")
//     private List<SensorLog> sensorLogs;

//     @OneToMany(mappedBy = "trashCan")
//     private List<CanStatusLog> statusLogs;

//     @OneToMany(mappedBy = "trashCan")
//     private List<CollectionHistory> collectionHistories;
// }

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
}