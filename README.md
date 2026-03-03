# ✈️ 비행계획 자동생성 시스템 (Flight Plan Auto-Generator)

> 해군 항공대 비행계획 초안 자동 생성 및 편집 시스템 프로토타입

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)
![React](https://img.shields.io/badge/React-18-blue)
![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue)
![SQLite](https://img.shields.io/badge/SQLite-3-lightgrey)

## 🎯 프로젝트 목표

**"비행계획 초안 자동 생성 → 사용자 편집/확정"** 데모로 요구사항 협의 가능 수준 확보

- 대상: 해군 포항/제주 기지 조종사/승무원
- 핵심차별: 규칙/가중치 조절 → 재계산 → 스코어/위반/사유 "미리보기"
- AI요소: "제약충족 + 가중치 스코어" 기반 휴리스틱 + 로컬서치

## 📁 프로젝트 구조

```
flight-plan-demo/
├── 📁 backend/                 # Spring Boot 백엔드
│   ├── src/main/java/com/flightplan/demo/
│   │   ├── 📁 entity/          # JPA 엔티티 (14개)
│   │   ├── 📁 repository/      # 데이터 접근 계층
│   │   ├── 📁 service/         # 비즈니스 로직
│   │   ├── 📁 controller/      # REST API
│   │   ├── 📁 dto/             # 데이터 전송 객체
│   │   ├── 📁 algorithm/       # Greedy + Local Search
│   │   │   ├── FlightPlanAlgorithmService.java
│   │   │   └── ConstraintChecker.java
│   │   └── 📁 config/          # 설정/초기화
│   └── pom.xml
│
├── 📁 frontend/                # React + TypeScript + Vite
│   ├── src/
│   │   ├── 📁 components/      # 재사용 컴포넌트
│   │   ├── 📁 pages/           # 페이지 컴포넌트
│   │   │   ├── Dashboard.tsx
│   │   │   ├── Personnel.tsx
│   │   │   ├── Assets.tsx
│   │   │   ├── Templates.tsx
│   │   │   ├── DraftGenerator.tsx
│   │   │   └── DraftEditor.tsx
│   │   ├── 📁 api/             # API 클라이언트
│   │   └── 📁 types/           # TypeScript 타입
│   └── package.json
│
└── 📄 README.md
```

## 🛠 기술 스택

### Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 | 언어 |
| Spring Boot | 3.2 | 프레임워크 |
| Spring Data JPA | 3.2 | ORM |
| SQLite | 3.44 | 데이터베이스 |
| Lombok | 1.18 | 보일러플레이트 감소 |
| Maven | 3.9 | 빌드 도구 |

### Frontend
| 기술 | 버전 | 용도 |
|------|------|------|
| React | 18.2 | UI 라이브러리 |
| TypeScript | 5.2 | 타입 시스템 |
| Vite | 5.0 | 빌드 도구 |
| React Query | 3.39 | 데이터 페칭 |
| React Router | 6.20 | 라우팅 |
| date-fns | 2.30 | 날짜 처리 |

## 🚀 시작하기

### 필수 조건
- Java 17+
- Node.js 18+
- Maven 3.9+

### 1. 백엔드 실행

```bash
cd backend

# 의존성 설치 및 빌드
mvn clean install

# 실행
mvn spring-boot:run
```

백엔드가 `http://localhost:8080/api` 에서 실행됩니다.

### 2. 프론트엔드 실행

```bash
cd frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
```

프론트엔드가 `http://localhost:3000` 에서 실행됩니다.

### 3. 더미 데이터

앱 첫 실행 시 자동으로 생성됩니다:
- 포항/제주 기지 (각 40명 인원, 총 80명)
- 기체 40대 (고정익/회전익 각 20대)
- 임무 템플릿 10개
- 랜덤 부재/자격 데이터

## ✨ 핵심 기능

### 1. 🤖 AI 기반 비행계획 생성

**Greedy + Local Search 알고리즘**

```
1단계: Greedy로 feasible 초안 생성
   └─ 하드 제약 확인 (부재/자격/중복)
   └─ 평가 함수로 최적 인원 선택

2단계: Local Search로 최적화
   └─ 인원 간 스왑 시도
   └─ 스코어 개선 시 적용
   └─ 수렴 시까지 반복
```

**스코어 계산식:**
```
TotalScore = Σ(라인 점수) - Σ(소프트 위반 페널티)

라인 점수 = skillW × 숙련도 + fairnessW × 공평성 
          - fatigueW × 피로도 - continuityW × 연속배정
```

### 2. ⚖️ 가중치 기반 재계산

| 가중치 | 설명 | 효과 |
|--------|------|------|
| 공평성 | 업무 분산 정도 | 높을수록 고르게 배정 |
| 숙련도 | 경험 우선 | 높을수록 숙련자 선호 |
| 피로도 | 휴식 보장 | 높을수록 연속 배정 회피 |
| 연속성 | 연속 근무 억제 | 높을수록 휴식 확보 |

### 3. 📊 미리보기 (Score Breakdown)

각 배정에 대해 설명 가능한 점수 제공:
```
"김조종사 배정 이유:
- 숙련도 기여: +8 (총 비행 2000시간)
- 공평성 기여: +4 (이번 달 2회 배정)
- 피로도 페널티: -1 (어제 배정)
- 총점: +11"
```

### 4. ⚠️ 위반 사항 실시간 감지

**하드 제약 (절대 위반 금지):**
- 부재 중 배정 금지
- 필수 자격 미충족 금지
- 동일 시간 중복 배정 금지

**소프트 제약 (가중치 기반 페널티):**
- 공평성 위반 (특정 인원 과다 배정)
- 피로도 위반 (연속/야간/주말)

## 🖥 화면 구성

| 화면 | 기능 | 경로 |
|------|------|------|
| 대시보드 | 월별 요약, 통계 차트, 일별 현황 | `/` |
| 인원 관리 | 조종사/승무원 CRUD, 검색/필터 | `/personnel` |
| 기체 관리 | 기체 현황, 가용성 관리 | `/assets` |
| 임무 템플릿 | 템플릿 정의/수정 | `/templates` |
| 계획 생성 | 초안 생성 설정 | `/drafts` |
| 계획 편집 | 일정 표, 스코어 패널, 재계산 | `/drafts/:id` |

## 🔌 API 엔드포인트

### 비행계횝
```http
POST   /api/plans/draft/generate          # 초안 생성
GET    /api/plans/draft/{id}              # 초안 조회
PATCH  /api/plans/draft/{id}/items/{itemId}  # 항목 수정
POST   /api/plans/draft/{id}/recompute    # 재계산
GET    /api/plans/summary/month           # 월별 요약
```

### 인원/자산
```http
GET    /api/personnel                     # 인원 목록
GET    /api/aircrafts                     # 기체 목록
GET    /api/templates                     # 임무 템플릿
```

### 설정
```http
GET    /api/config/weights                # 가중치 설정 목록
PUT    /api/config/weights/{id}           # 가중치 설정 수정
```

## 🎬 데모 시나리오

### 시나리오 1: 정상 계획 생성
```
1. 대시보드에서 기지/월 선택
2. "비행계획" 메뉴에서 "계획 생성" 클릭
3. 기간(주간)/기종(고정익)/가중치(기본) 설정
4. 생성된 초안 확인 (24건 자동 생성)
5. 수동 수정 후 저장
```

### 시나리오 2: 긴급 상황 (부재 등록)
```
1. 특정 인원 부재 등록 (휴가/교육/병가)
2. 기존 계획에서 "재계산" 클릭
3. "공평성 우선" 가중치 선택
4. 대체 인원 자동 추천 확인
5. 수정된 계획 저장
```

### 시나리오 3: 가중치 비교
```
1. 동일 조건으로 계획 생성
2. 총점 확인 (예: 296.6점)
3. "재계산" → "숙련도 우선" 선택
4. 스코어 변화 확인 (예: 156.8점)
5. 전/후 배정 인원 비교
```

## 📋 향후 개선 사항 (TODO)

### 🔐 인증/보안
- [ ] JWT 기반 로그인/로그아웃
- [ ] 역할 기반 접근 제어 (RBAC)
- [ ] 조종사/관리자 권한 분리
- [ ] 비밀번호 암호화 (BCrypt)

### 👥 인원 관리 고도화
- [ ] 인원 상세 페이지 (프로필)
- [ ] 자격증 관리 (만료 알림)
- [ ] 부재 관리 전용 페이지
- [ ] 교육 이력 관리
- [ ] 엑셀 일괄 업로드

### 📝 비행계획 고도화
- [ ] 드래그 앤 드롭 인원 배정
- [ ] 실시간 충돌 감지
- [ ] 비행 일정 복사/붙여넣기
- [ ] 반복 일정 설정
- [ ] 긴급 대체 인원 추천

### 📊 시각화/리포트
- [ ] 차트 라이브러리 (Recharts → D3.js)
- [ ] 인원별 비행 시간 추이
- [ ] 기체 가동률 통계
- [ ] PDF 리포트 출력
- [ ] 엑셀 다운로드

### 🔔 알림/연동
- [ ] WebSocket 실시간 알림
- [ ] 위반 사항 실시간 경고
- [ ] SMS/Email 알림 연동
- [ ] 달력 연동 (Google/Outlook)

### ⚡ 성능/확장성
- [ ] 데이터베이스 마이그레이션 (MySQL/PostgreSQL)
- [ ] Redis 캐싱
- [ ] 대용량 데이터 처리 (배치)
- [ ] 다중 기지 동시 계획 생성

### 🧪 테스트
- [ ] 단위 테스트 (JUnit)
- [ ] 통합 테스트
- [ ] E2E 테스트 (Cypress)
- [ ] 성능 테스트 (JMeter)

## 🐛 알려진 이슈

1. **프론트엔드**: 새로고침 시 404 오류 (Vite dev server 설정 필요)
2. **알고리즘**: Local Search 반복 횟수 제한으로 인해 최적해가 아닌 근사해 반환
3. **데이터**: 대용량 데이터(1개월+) 처리 시 성능 저하

## 📄 라이선스

MIT License

## 👥 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 문의

프로젝트 관련 문의: [GitHub Issues](https://github.com/yourusername/flight-plan-demo/issues)
