package com.linuxmaster.jeongbo;

import com.linuxmaster.ui.CliPrinter;

import java.util.*;

public class JeongboRunner {

    private final McqEngine engine = new McqEngine();
    private final McqProgressStore store = new McqProgressStore();
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        CliPrinter.header("정보처리기사 필기 기출문제");

        engine.loadAllYears(new int[]{2020, 2021, 2022, 2023, 2024});
        CliPrinter.info("✅ 총 " + engine.totalCount() + "문제 로드 완료 (2020~2024년 기출)");

        while (true) {
            showMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> startQuiz(engine.getRandomSet(20));
                case "2" -> selectSubjectAndQuiz();
                case "3" -> selectYearRoundAndQuiz();
                case "4" -> reviewWrongNotes();
                case "5" -> showProgress();
                case "b", "B" -> { return; }
                default -> CliPrinter.error("올바른 메뉴를 선택해주세요.");
            }
        }
    }

    // ─── 메인 메뉴 ────────────────────────────────────────────────

    private void showMainMenu() {
        System.out.println();
        CliPrinter.separator();
        CliPrinter.plain("  1) 랜덤 문제 풀기 (20문제)");
        CliPrinter.plain("  2) 과목별 문제 풀기");
        CliPrinter.plain("  3) 연도/회차별 문제 풀기");
        CliPrinter.plain("  4) 오답노트 복습");
        CliPrinter.plain("  5) 진도 현황 보기");
        CliPrinter.plain("  b) 시험 선택으로 돌아가기");
        CliPrinter.separator();
        System.out.print("선택 > ");
    }

    // ─── 퀴즈 진행 ────────────────────────────────────────────────

    private void startQuiz(List<McqQuestion> questions) {
        if (questions.isEmpty()) { CliPrinter.error("문제가 없습니다."); return; }

        int total = questions.size();
        int correct = 0;

        for (int i = 0; i < total; i++) {
            McqQuestion q = questions.get(i);

            printQuestion(i + 1, total, q);

            Integer userAnswer = null;
            boolean skipped = false;

            while (userAnswer == null && !skipped) {
                CliPrinter.prompt();
                String input = scanner.nextLine().trim();

                switch (input.toLowerCase()) {
                    case "h" -> CliPrinter.hint(q.hint != null ? q.hint : "힌트가 없습니다.");
                    case "s" -> { CliPrinter.info("건너뜁니다."); skipped = true; }
                    case "q" -> { CliPrinter.info("퀴즈를 종료합니다."); return; }
                    default -> {
                        try {
                            int n = Integer.parseInt(input);
                            if (n < 1 || n > 4) {
                                CliPrinter.error("1~4 사이의 숫자를 입력하세요.");
                            } else {
                                userAnswer = n;
                            }
                        } catch (NumberFormatException e) {
                            CliPrinter.error("1~4 숫자 또는 h/s/q를 입력하세요.");
                        }
                    }
                }
            }

            if (skipped) continue;

            store.recordResult(q, userAnswer);

            if (userAnswer == q.answer) {
                CliPrinter.correct("✅ 정답!");
                correct++;
            } else {
                CliPrinter.wrong("❌ 오답! 정답은 " + q.answer + "번입니다.");
            }

            printExplanation(q);

            if (i < total - 1) {
                System.out.print("\n엔터를 눌러 다음 문제로...");
                scanner.nextLine();
            }
        }

        CliPrinter.header(String.format("퀴즈 완료! 정답 %d / %d", correct, total));
        CliPrinter.stats(store.getSolvedIds().size(), engine.totalCount(), store.getAccuracy());
    }

    // ─── 문제 출력 ────────────────────────────────────────────────

    private void printQuestion(int current, int total, McqQuestion q) {
        System.out.println();
        System.out.printf("\033[1m[문제 %d/%d]\033[0m \033[36m과목: %s\033[0m \033[90m| 난이도: %s\033[0m%n",
            current, total, q.subject, q.difficulty);
        System.out.println("\033[1m\033[36m─────────────────────────────────────────────────\033[0m");
        System.out.println("\033[1m\033[97m" + q.question + "\033[0m");
        System.out.println("\033[1m\033[36m─────────────────────────────────────────────────\033[0m");
        for (int j = 0; j < q.options.size(); j++) {
            System.out.printf("  \033[1m%d)\033[0m %s%n", j + 1, q.options.get(j));
        }
        System.out.println();
        System.out.println("\033[90m  [h] 힌트 보기  [s] 건너뛰기  [q] 종료\033[0m");
    }

    private void printExplanation(McqQuestion q) {
        System.out.println();
        System.out.println("\033[1m📖 해설\033[0m");
        System.out.println("\033[32m  정답: " + q.answer + "번 — " + q.options.get(q.answer - 1) + "\033[0m");
        if (q.explanation != null && !q.explanation.isBlank()) {
            System.out.println("\033[90m  " + q.explanation + "\033[0m");
        }
    }

    // ─── 과목별 퀴즈 ─────────────────────────────────────────────

    private void selectSubjectAndQuiz() {
        List<String> subjects = new ArrayList<>(engine.getSubjects());
        System.out.println("\n과목을 선택하세요:");
        for (int i = 0; i < subjects.size(); i++) {
            long count = engine.getBySubject(subjects.get(i)).size();
            System.out.printf("  %d) %-20s (%d문제)%n", i + 1, subjects.get(i), count);
        }
        System.out.print("선택 > ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx < 0 || idx >= subjects.size()) { CliPrinter.error("잘못된 선택입니다."); return; }
            startQuiz(engine.getBySubject(subjects.get(idx)));
        } catch (NumberFormatException e) {
            CliPrinter.error("숫자를 입력해주세요.");
        }
    }

    // ─── 연도/회차별 퀴즈 ────────────────────────────────────────

    private void selectYearRoundAndQuiz() {
        List<Integer> years = engine.getAvailableYears();
        System.out.println("\n연도를 선택하세요:");
        for (int i = 0; i < years.size(); i++) {
            int y = years.get(i);
            System.out.printf("  %d) %d년 (%d문제)%n", i + 1, y, engine.getByYear(y).size());
        }
        System.out.print("선택 > ");
        int yearIdx;
        try {
            yearIdx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (yearIdx < 0 || yearIdx >= years.size()) { CliPrinter.error("잘못된 선택입니다."); return; }
        } catch (NumberFormatException e) { CliPrinter.error("숫자를 입력해주세요."); return; }

        int year = years.get(yearIdx);
        System.out.println("\n회차를 선택하세요:");
        System.out.printf("  0) %d년 전체%n", year);
        for (int r = 1; r <= 3; r++) {
            int cnt = engine.getByYearRound(year, r).size();
            System.out.printf("  %d) %d회 (%d문제)%n", r, r, cnt);
        }
        System.out.print("선택 > ");
        try {
            int round = Integer.parseInt(scanner.nextLine().trim());
            List<McqQuestion> questions = (round == 0)
                ? engine.getByYear(year)
                : engine.getByYearRound(year, round);
            if (questions.isEmpty()) { CliPrinter.error("해당 회차 문제가 없습니다."); return; }
            startQuiz(questions);
        } catch (NumberFormatException e) { CliPrinter.error("숫자를 입력해주세요."); }
    }

    // ─── 오답노트 ────────────────────────────────────────────────

    private void reviewWrongNotes() {
        List<McqProgressStore.WrongNote> notes = store.getWrongNotes();
        if (notes.isEmpty()) { CliPrinter.info("오답노트가 비어있습니다. 훌륭해요! 🎉"); return; }

        CliPrinter.header("오답노트 (" + notes.size() + "문제)");
        for (int i = 0; i < notes.size(); i++) {
            McqProgressStore.WrongNote note = notes.get(i);
            System.out.printf("%n[%d] \033[36m%s\033[0m%n", i + 1, note.subject);
            System.out.println("  " + note.question);
            if (note.options != null) {
                for (int j = 0; j < note.options.size(); j++) {
                    String mark = (j + 1 == note.correctAnswer) ? " \033[32m← 정답\033[0m" : "";
                    String userMark = (j + 1 == note.userAnswer) ? " \033[31m← 내 답\033[0m" : "";
                    System.out.printf("    %d) %s%s%s%n", j + 1, note.options.get(j), mark, userMark);
                }
            }
            System.out.println("  해설: " + note.explanation);
            System.out.println("  기록: " + note.timestamp);
            CliPrinter.separator();
        }

        System.out.print("\n오답 문제만 다시 풀까요? (y/n) > ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            startQuiz(engine.getWrongQuestions(store.getWrongIds()));
        }
    }

    // ─── 진도 현황 ────────────────────────────────────────────────

    private void showProgress() {
        CliPrinter.header("나의 진도 현황 — 정보처리기사 필기");
        CliPrinter.stats(store.getSolvedIds().size(), engine.totalCount(), store.getAccuracy());
        System.out.printf("  총 시도:  %d회%n", store.getTotalAttempts());
        System.out.printf("  총 정답:  %d회%n", store.getTotalCorrect());
        System.out.printf("  오답노트: %d문제%n", store.getWrongNotes().size());

        if (!store.getWrongIds().isEmpty()) {
            System.out.println();
            System.out.println("  ⚠️  미해결 오답 ID: " + String.join(", ", store.getWrongIds()));
        }
    }
}
