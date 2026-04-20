package com.ecolink.backend.repository;

import com.ecolink.backend.entity.TrashCan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrashCanRepository extends JpaRepository<TrashCan, Long>{
    
}
