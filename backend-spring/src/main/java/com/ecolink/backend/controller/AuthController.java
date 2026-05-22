package com.ecolink.backend.controller;

import com.ecolink.backend.entity.Worker;
import com.ecolink.backend.repository.WorkerRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final WorkerRepository workerRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<Worker> user = workerRepository.findByUsernameAndPassword(username, password);

        if (user.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "아이디 또는 비밀번호가 틀렸습니다."));
        }

        session.setAttribute("userId", user.get().getId());
        session.setAttribute("username", user.get().getUsername());
        session.setAttribute("grade", user.get().getGrade());

        return ResponseEntity.ok(Map.of(
                "username", user.get().getUsername(),
                "grade", user.get().getGrade()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object userId = session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인 필요"));
        }
        return ResponseEntity.ok(Map.of(
                "username", session.getAttribute("username"),
                "grade", session.getAttribute("grade")));
    }
}