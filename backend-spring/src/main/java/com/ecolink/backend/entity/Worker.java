package com.ecolink.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")  // ← 변경
@Getter
@NoArgsConstructor
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "grade")
    private Integer grade;  // ← 추가

    @Column(name = "vehicle_number", length = 20)
    private String vehicleNumber;

    @JsonIgnore
    @OneToMany(mappedBy = "worker")
    private List<CollectionRoute> collectionRoutes;

    public Worker(String username, String password, Integer grade, String vehicleNumber){
        this.username=username;
        this.password=password;
        this.grade=grade;
        this.vehicleNumber=vehicleNumber;
    }
}