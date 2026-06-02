# 리눅스 마스터 1급 실기 연습 CLI

Docker 컨테이너(CentOS)를 활용해 실제 명령어를 실행하고 채점하는 CLI 학습 도구입니다.

## 사전 요구사항

- Java 17+
- Docker 설치 및 `linux-master` 컨테이너 실행 중

```bash
# 컨테이너 확인
docker ps | grep linux-master

# 컨테이너가 중지된 경우
docker start linux-master
```

## 빌드 & 실행

```bash
# IntelliJ에서: Gradle 패널 → Tasks → application → run
# 또는 터미널에서:
./gradlew run
```

## 실행 화면 예시

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  리눅스 마스터 1급 실기 연습
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✅ linux-master 컨테이너 연결 완료
✅ 문제 10개 로드 완료

─────────────────────────────────────────────────
  1) 랜덤 문제 풀기 (10문제)
  2) 카테고리별 문제 풀기
  3) 오답노트 복습
  4) 진도 현황 보기
  q) 종료

[문제 1/10] 카테고리: 파일시스템 | 난이도: 하
─────────────────────────────────────────────────
/etc/hosts 파일에서 'localhost' 문자열이 포함된 줄을 줄 번호와 함께 출력하시오.
─────────────────────────────────────────────────
  [h] 힌트 보기  [s] 건너뛰기  [q] 종료

> grep -n localhost /etc/hosts

⏳ 실행 중...
┌── 실행 결과 ─────────────────────────────────────
│  1:  127.0.0.1   localhost localhost.localdomain
└──────────────────────────────────────────────────

✅ 정답!

📖 해설
  모범 답안: grep -n 'localhost' /etc/hosts
  -n 옵션은 매칭된 줄의 번호를 함께 출력합니다.
```

## 프로젝트 구조

```
src/main/java/com/linuxmaster/
├── Main.java              # 진입점
├── app/AppRunner.java     # 메인 루프 & 메뉴
├── quiz/
│   ├── QuizEngine.java    # 문제 로드 & 필터링
│   ├── QuizQuestion.java  # 문제 모델
│   └── QuizResult.java    # 결과 모델
├── grader/
│   └── CommandGrader.java # 채점 엔진
├── sandbox/
│   └── DockerSandbox.java # Docker 명령어 실행
├── storage/
│   └── ProgressStore.java # 진도/오답 JSON 저장
└── ui/
    └── CliPrinter.java    # 컬러 CLI 출력

src/main/resources/questions/
└── linux1급_commands.json # 문제 데이터
```

## 문제 추가 방법

`src/main/resources/questions/linux1급_commands.json` 파일에 다음 형식으로 추가:

```json
{
  "id": "고유ID",
  "category": "카테고리명",
  "difficulty": "하|중|상",
  "question": "문제 본문",
  "hint": "힌트",
  "answers": ["정답1", "정답2"],
  "explanation": "해설",
  "expectedOutput": "",
  "useOutputGrading": false
}
```

## 채점 방식

| 방식 | 설명 |
|------|------|
| 명령어 일치 채점 | answers 목록과 비교 (`useOutputGrading: false`) |
| 출력 결과 채점 | 실행 결과를 expectedOutput과 비교 (`useOutputGrading: true`) |
