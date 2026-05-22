package com.ecolink.backend.service;

import org.springframework.transaction.annotation.Transactional;
import com.ecolink.backend.dto.TrashCanRequest;
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

    // 추가
    public TrashCan create(TrashCanRequest request) {
        // 새 쓰레기통 생성 후 DB에 저장
        TrashCan trashCan = new TrashCan(
                request.getLocName(), // 위치 이름
                request.getLocLat(), // 위도
                request.getLocLng(), // 경도
                request.getMaxCapa() // 최대 용량
        );
        return trashCanRepository.save(trashCan);
    }

    @Transactional
    public TrashCan update(Long id, TrashCanRequest request) {
        // 기존 쓰레기통 정보 수정 후 DB에 반영
        // 수정 중 오류 나면 자동으로 되돌리기
        TrashCan trashCan = findById(id); // id로 해당 쓰레기통 찾기
        trashCan.update( // 새 값으로 업데이트
                request.getLocName(), // 위치 이름
                request.getLocLat(), // 위도
                request.getLocLng(), // 경도
                request.getMaxCapa() // 최대 용량
        );
        return trashCan; // 수정된 쓰레기통 반환
    }

    public void delete(Long id) { // 쓰레기통 삭제
        TrashCan trashCan = findById(id); // id로 해당 쓰레기통 찾기
        trashCanRepository.delete(trashCan); // DB에서 삭제
    }
}