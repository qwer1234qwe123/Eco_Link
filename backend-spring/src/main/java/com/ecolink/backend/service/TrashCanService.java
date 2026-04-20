package com.ecolink.backend.service;

import com.ecolink.backend.entity.TrashCan;
import com.ecolink.backend.repository.TrashCanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrashCanService {

    private final TrashCanRepository trashCanRepository;

    public List<TrashCan> findAll() {
        return trashCanRepository.findAll();
    }

    public TrashCan findById(Long id) {
        return trashCanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("쓰레기통을 찾을 수 없습니다. id: " + id));
    }
}