package com.ecolink.backend.service;

import com.ecolink.backend.dto.SignUpRequest;
import com.ecolink.backend.entity.Worker;
import com.ecolink.backend.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerService{
    private final WorkerRepository WorkerRepository;

    public List<Worker> findAll(){
        return WorkerRepository.findAll();
    }

    public Worker findById(Long id){
        return WorkerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("작업자를 찾을 수 없습니다. id: " + id));
    }

    public void signUp(SignUpRequest request){
        Worker worker=new Worker(
            request.getUsername(),
            request.getPassword(),
            5,
            request.getVehicleNumber()
        );
        WorkerRepository.save(worker);
    }

    public void update(Long id, SignUpRequest request){
        Worker worker = WorkerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("작업자를 찾을 수 없습니다. id: " + id));
        worker.update(request.getUsername(), request.getPassword(), request.getVehicleNumber());
        WorkerRepository.save(worker);
    }

    public void delete(Long id){
        WorkerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("작업자를 찾을 수 없습니다. id: " + id));
        WorkerRepository.deleteById(id);
    }
}