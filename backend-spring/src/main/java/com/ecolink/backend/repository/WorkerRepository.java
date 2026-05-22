package com.ecolink.backend.repository;

import com.ecolink.backend.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    Optional<Worker> findByUsernameAndPassword(String username, String password);
}
