# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

**EcoLink**는 AI 기반 폐기물 수거 예측 및 경로 최적화 시스템입니다.
초음파 센서로 쓰레기통 적재율을 수집 → DB 저장 → AI 포화 시점 예측 → 웹 시각화로 이어지는 통합 IoT+AI+Web 프로젝트입니다.

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Front-End | HTML, CSS, JavaScript, AJAX |
| Back-End | Java, Spring Boot |
| AI | Python, PyTorch (FastAPI 연동) |
| DB | MariaDB |
| IoT/Hardware | ESP32 또는 Wi-Fi 내장 아두이노, 초음파 센서 |
| 서버 | VMware vSphere 기반 Ubuntu (학교 제공) |
| 협업 | GitHub, VS Code, STS (Spring Tools for Eclipse) |

---

## 팀원 역할 분담

### 이서준 — Front-End
- HTML, CSS, JavaScript, AJAX 사용
- 웹 페이지 UI 구성
- 쓰레기통 상태 및 예측 결과 표시
- 수거 대상 및 추천 경로 시각화
- 백엔드 API 연동

### 강대웅 — Back-End 1
- Java, Spring Boot 사용
- 센서 데이터 수신 API 개발
- 프론트엔드 연동 API 개발
- 수거 대상 조회 및 예측 결과 조회 API 개발
- 전체 서버 기능 구현

### 허찬 — Back-End 2 + DB 유지보수
- Java, Spring Boot 사용
- 데이터베이스 연동
- DB 수정 및 유지보수, 테이블 구조 관리
- 센서 데이터·예측 결과·수거 기록 저장 구조 관리
- 백엔드 기능 보조

### 전영준 — AI
- Python, PyTorch 사용
- 수집 데이터 전처리
- 쓰레기통 포화 시점 예측 모델 개발
- 오후 6시 이전 임계치 도달 여부 판단
- 수거 대상 선별 기준 반영
- 예측 결과 FastAPI 서버 연동 준비

### 박승국 — IoT / Hardware
- ESP32 또는 Wi-Fi 내장 아두이노, 초음파 센서 사용
- 초음파 센서 데이터 측정
- 장비 설치 및 배선 구성
- 센서값 수집 및 서버 전송
- 현장 테스트 및 장비 동작 확인

---

## 프로젝트 구조

```
EcoLink-project/
├── backend-spring/          # Java/Spring Boot 백엔드
│   ├── build.gradle
│   └── src/main/java/
│       ├── controller/      # API 엔드포인트
│       ├── service/         # 비즈니스 로직 + AI 서버 통신 (RestTemplate)
│       ├── repository/      # MariaDB 접근 (JPA)
│       └── entity/          # DB 테이블 매핑
│   └── src/main/resources/
│       ├── static/          # (선택) 프론트 통합 시 index.html, style.css, script.js
│       └── application.yml  # DB 연결 설정
│
├── frontend/                # HTML/CSS/JS 프론트엔드 (또는 static 폴더로 통합)
│   ├── index.html
│   ├── style.css
│   └── script.js
│
├── ai-model/                # Python AI 모델 서버
│   ├── venv/
│   ├── train.py             # 모델 학습 (MariaDB 데이터 활용)
│   ├── main.py              # FastAPI 서버 (Spring과 통신)
│   ├── my_model.pkl         # 학습 완료 모델
│   └── requirements.txt
│
├── arduino/                 # ESP32/아두이노 센서 코드
│   └── sensor_sender.ino    # 초음파 센서 측정 + HTTP 전송
│
└── schema.sql               # MariaDB 테이블 생성 쿼리
```

---

## 시스템 흐름

```
[아두이노 + 초음파 센서]
        │  HTTP POST (센서 데이터 전송)
        ▼
[Spring Boot 백엔드] ──────────────────────────────────────────────────
        │  JPA 저장                      │  RestTemplate 호출
        ▼                                ▼
[MariaDB]                       [FastAPI AI 서버]
 - trash_can                      - 누적 데이터 학습/예측
 - sensor_log                     - 포화 시점 반환
 - empty_history                  - 수거 대상 선별
 - prediction_log
 - collection_route
 - worker / vehicle
        │
        │  REST API 응답 (JSON)
        ▼
[프론트엔드 (HTML/JS/AJAX)]
 - 쓰레기통 현재 상태 표시
 - 예측 결과 시각화
 - 수거 대상 및 추천 경로 지도 표시
```

---

## DB 핵심 테이블 (MariaDB)

| 테이블 | 설명 |
|--------|------|
| `trash_can` | 쓰레기통 위치, 용량, 상태 |
| `sensor_log` | 실시간 적재율·배터리·overflow 로그 |
| `empty_history` | 비움 전후 적재율, 비운 시각 |
| `prediction_log` | AI 예측 결과, 신뢰도, 예측 생성 시각 |
| `collection_route` | 수거 경로 (worker, vehicle 연결) |
| `worker` | 수거 담당자 계정 정보 |
| `vehicle` | 수거 차량 위치 및 상태 |

AI 모델 학습의 핵심 데이터: 센서 적재율 변화 기록, 비움 시점, 비우기 전후 적재율, 예측 생성 시각, 신뢰도

---

## 빌드 및 실행 명령어

**백엔드 (Spring Boot)** — `backend-spring/` 디렉토리에서 실행:
```bash
cd backend-spring
./gradlew build        # 빌드
./gradlew test         # 전체 테스트 (JUnit 5)
./gradlew bootRun      # 서버 실행
./gradlew clean build  # 클린 빌드
./gradlew test --tests "com.ecolink.backend.SomeTestClass"  # 단일 테스트
```

**AI 서버 (FastAPI)** — `ai-model/` 디렉토리에서 실행:
```bash
cd ai-model
pip install -r requirements.txt
python main.py
```

---

## 코드 작성 시 주의사항

- 학교 팀 프로젝트 수준에 맞게 **이해하기 쉽고 실현 가능한 구조** 우선
- 기업형 과도한 추상화보다 **직관적인 레이어 구조** 유지
- Front-End, Back-End, AI, IoT, DB가 **하나의 연결된 시스템**으로 동작
- 역할 분담(이서준/강대웅/허찬/전영준/박승국)을 항상 고려하여 코드 분리
- AI 서버는 FastAPI로 포트 분리 운영 (Spring에서 RestTemplate으로 호출)
- DB 설계 변경 시 허찬(DB 담당)과 협의 필요

## 언어 설정
모든 응답은 한국어로 작성할 것