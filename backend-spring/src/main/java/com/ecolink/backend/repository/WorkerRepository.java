package com.ecolink.backend.repository;

import com.ecolink.backend.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Long>{
    
}
