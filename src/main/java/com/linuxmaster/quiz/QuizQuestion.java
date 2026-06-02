package com.linuxmaster.quiz;

import java.util.List;

public class QuizQuestion {

    public String id;
    public String category;          // 예: "파일시스템", "프로세스", "네트워크"
    public String difficulty;        // "하" | "중" | "상"
    public String question;          // 문제 본문
    public String hint;              // 힌트
    public List<String> answers;     // 정답 목록 (여러 정답 허용)
    public String explanation;       // 해설
    public String setupCommand;      // 문제 실행 전 환경 세팅 명령어 (선택)
    public String expectedOutput;    // 예상 출력 결과 (채점에 사용)
    public String gradingCommand;    // 출력 비교용 별도 명령어 (없으면 사용자 명령어 그대로 실행)
    public boolean useOutputGrading; // true면 실행결과로 채점, false면 명령어 문자열로 채점

    @Override
    public String toString() {
        return "[" + category + "/" + difficulty + "] " + question;
    }
}
