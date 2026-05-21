TrashCanService.java

package com.ecolink.backend.service;

import com.ecolink.backend.entity.TrashCan;
import com.ecolink.backend.repository.TrashCanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
// 추가
import com.ecolink.backend.dto.TrashCanRequest;
import org.springframework.transaction.annotation.Transactional;

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


    // 추가
    public TrashCan create(TrashCanRequest request) {
    TrashCan trashCan = new TrashCan(
        request.getLocName(),
        request.getLocLat(),
        request.getLocLng(),
        request.getMaxCapa()
    );
    return trashCanRepository.save(trashCan);
}

@Transactional
public TrashCan update(Long id, TrashCanRequest request) {
    TrashCan trashCan = findById(id);
    trashCan.update(
        request.getLocName(),
        request.getLocLat(),
        request.getLocLng(),
        request.getMaxCapa()
    );
    return trashCan;
}

public void delete(Long id) {
    TrashCan trashCan = findById(id);
    trashCanRepository.delete(trashCan);
}
}