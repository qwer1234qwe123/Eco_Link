// package com.ecolink.backend.entity;

// import jakarta.persistence.*;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import java.time.LocalDateTime;

// @Entity
// @Table(name = "sensor_log")
// @Getter
// @NoArgsConstructor
// public class SensorLog {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "can_id", nullable = false)
//     private TrashCan trashCan;

//     @Column(name = "fill_level", nullable = false)
//     private Integer fillLevel;

//     @Column(name = "battery_level", nullable = false)
//     private Integer batteryLevel;

//     @Column(name = "log_time", nullable = false)
//     private LocalDateTime logTime;

//     @PrePersist
//     public void prePersist() {
//         this.logTime = LocalDateTime.now();
//     }

//     public SensorLog(TrashCan trashCan, Integer fillLevel, Integer batteryLevel) {
//         this.trashCan = trashCan;
//         this.fillLevel = fillLevel;
//         this.batteryLevel = batteryLevel;
//     }
// }

package com.ecolink.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "sensor_log")
@Getter
@NoArgsConstructor
public class SensorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "can_id", nullable = false)
    private TrashCan trashCan;

    @Column(name = "can_id", insertable = false, updatable = false)
    private Long canId;

    @Column(name = "fill_level", nullable = false)
    private Integer fillLevel;

    @Column(name = "battery_level", nullable = false)
    private Integer batteryLevel;

    @Column(name = "log_time", nullable = false)
    private LocalDateTime logTime;

    @PrePersist
    public void prePersist() {
        this.logTime = LocalDateTime.now();
    }

    public SensorLog(TrashCan trashCan, Integer fillLevel, Integer batteryLevel) {
        this.trashCan = trashCan;
        this.fillLevel = fillLevel;
        this.batteryLevel = batteryLevel;
    }
}