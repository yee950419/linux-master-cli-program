package com.linuxmaster.jeongbo;

import java.util.List;

public class McqQuestion {

    public String id;
    public String subject;      // 과목: 소프트웨어 설계, 소프트웨어 개발, 데이터베이스 구축, 프로그래밍 언어 활용, 정보시스템 구축 관리
    public String topic;        // 세부 주제
    public String difficulty;   // 하 | 중 | 상
    public int year;            // 출제 연도
    public int round;           // 회차
    public String question;     // 문제 본문
    public List<String> options; // 보기 4개 (인덱스 0 = 1번)
    public int answer;           // 정답 번호 (1~4)
    public String hint;
    public String explanation;

    @Override
    public String toString() {
        return "[" + subject + "] " + question;
    }
}
