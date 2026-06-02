package com.linuxmaster.app;

import com.linuxmaster.concept.ConceptViewer;
import com.linuxmaster.grader.CommandGrader;
import com.linuxmaster.quiz.*;
import com.linuxmaster.sandbox.DockerSandbox;
import com.linuxmaster.storage.ProgressStore;
import com.linuxmaster.storage.ProgressStore.WrongNote;
import com.linuxmaster.ui.CliPrinter;

import java.util.*;

public class AppRunner {

    private final DockerSandbox sandbox = new DockerSandbox();
    private final CommandGrader grader = new CommandGrader(sandbox);
    private final QuizEngine engine = new QuizEngine();
    private final ConceptViewer conceptViewer = new ConceptViewer();
    private final ProgressStore store = new ProgressStore();
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        CliPrinter.header("리눅스 마스터 1급 실기 연습");

        if (!sandbox.isContainerRunning()) {
            CliPrinter.error("'linux-master' 컨테이너가 실행 중이지 않습니다.");
            CliPrinter.info("  docker start linux-master 으로 먼저 시작해주세요.");
            return;
        }
        CliPrinter.info("✅ linux-master 컨테이너 연결 완료");

        engine.loadQuestions("/questions/linux1급_commands.json");
        CliPrinter.info("✅ 문제 " + engine.totalCount() + "개 로드 완료");

        conceptViewer.load("/concepts/concepts.json");
        CliPrinter.info("✅ 개념 정리 " + conceptViewer.totalCount() + "개 카테고리 로드 완료");

        while (true) {
            showMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> startQuiz(engine.getRandomSet(10));
                case "2" -> selectCategoryAndQuiz();
                case "3" -> reviewWrongNotes();
                case "4" -> showProgress();
                case "5" -> showConceptMenu();
                case "q", "Q" -> { CliPrinter.info("\n👋 종료합니다. 수고하셨습니다!"); return; }
                default -> CliPrinter.error("올바른 메뉴를 선택해주세요.");
            }
        }
    }

    // ─── 메인 메뉴 ────────────────────────────────────────────────

    private void showMainMenu() {
        System.out.println();
        CliPrinter.separator();
        CliPrinter.plain("  1) 랜덤 문제 풀기 (10문제)");
        CliPrinter.plain("  2) 카테고리별 문제 풀기");
        CliPrinter.plain("  3) 오답노트 복습");
        CliPrinter.plain("  4) 진도 현황 보기");
        CliPrinter.plain("  5) 개념 정리 보기");
        CliPrinter.plain("  q) 종료");
        CliPrinter.separator();
        System.out.print("선택 > ");
    }

    // ─── 퀴즈 진행 ────────────────────────────────────────────────

    private void startQuiz(List<QuizQuestion> questions) {
        if (questions.isEmpty()) { CliPrinter.error("문제가 없습니다."); return; }

        int total = questions.size();
        int correct = 0;

        for (int i = 0; i < total; i++) {
            QuizQuestion q = questions.get(i);

            // 환경 세팅
            if (q.setupCommand != null && !q.setupCommand.isBlank()) {
                CliPrinter.info("🔧 환경 세팅 중...");
                grader.setup(q);
            }

            CliPrinter.question(i + 1, total, q.category, q.difficulty, q.question);

            QuizResult result = null;
            boolean skipped = false;

            while (result == null && !skipped) {
                CliPrinter.prompt();
                String input = scanner.nextLine().trim();

                switch (input.toLowerCase()) {
                    case "h" -> CliPrinter.hint(q.hint);
                    case "s" -> { CliPrinter.info("건너뜁니다."); skipped = true; }
                    case "q" -> { CliPrinter.info("퀴즈를 종료합니다."); return; }
                    default -> {
                        CliPrinter.info("\n⏳ 실행 중...");
                        result = grader.grade(q, input);
                    }
                }
            }

            if (skipped) continue;

            // 실행 결과 출력
            if (result.actualOutput != null && !result.actualOutput.isEmpty()) {
                CliPrinter.executionOutput(result.actualOutput);
            }

            // 채점 결과
            switch (result.grade) {
                case CORRECT -> { CliPrinter.correct(result.feedback); correct++; }
                case PARTIAL -> CliPrinter.partial(result.feedback);
                case WRONG   -> CliPrinter.wrong(result.feedback);
            }

            // 해설
            String modelAnswer = q.answers.isEmpty() ? "(없음)" : q.answers.get(0);
            CliPrinter.explanation(modelAnswer, q.explanation);
            store.recordResult(result);

            if (i < total - 1) {
                System.out.print("\n엔터를 눌러 다음 문제로...");
                scanner.nextLine();
            }
        }

        CliPrinter.header(String.format("퀴즈 완료! 정답 %d / %d", correct, total));
        CliPrinter.stats(store.getSolvedIds().size(), engine.totalCount(), store.getAccuracy());
    }

    // ─── 카테고리별 퀴즈 ─────────────────────────────────────────

    private void selectCategoryAndQuiz() {
        List<String> catList = new ArrayList<>(engine.getCategories());
        System.out.println("\n카테고리를 선택하세요:");
        for (int i = 0; i < catList.size(); i++) {
            long count = engine.getByCategory(catList.get(i)).size();
            System.out.printf("  %d) %-15s (%d문제)%n", i + 1, catList.get(i), count);
        }
        System.out.print("선택 > ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx < 0 || idx >= catList.size()) { CliPrinter.error("잘못된 선택입니다."); return; }
            startQuiz(engine.getByCategory(catList.get(idx)));
        } catch (NumberFormatException e) {
            CliPrinter.error("숫자를 입력해주세요.");
        }
    }

    // ─── 오답노트 ────────────────────────────────────────────────

    private void reviewWrongNotes() {
        List<WrongNote> notes = store.getWrongNotes();
        if (notes.isEmpty()) { CliPrinter.info("오답노트가 비어있습니다. 훌륭해요! 🎉"); return; }

        CliPrinter.header("오답노트 (" + notes.size() + "문제)");
        for (int i = 0; i < notes.size(); i++) {
            WrongNote note = notes.get(i);
            System.out.printf("%n[%d] %s%n", i + 1, note.question);
            System.out.println("  내 답:    " + note.userAnswer);
            System.out.println("  모범 답안: " + note.correctAnswer);
            System.out.println("  해설:     " + note.explanation);
            System.out.println("  기록 시각: " + note.timestamp);
            CliPrinter.separator();
        }

        System.out.print("\n오답 문제만 다시 풀까요? (y/n) > ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            startQuiz(engine.getWrongQuestions(store.getWrongIds()));
        }
    }

    // ─── 진도 현황 ────────────────────────────────────────────────

    private void showProgress() {
        CliPrinter.header("나의 진도 현황");
        CliPrinter.stats(store.getSolvedIds().size(), engine.totalCount(), store.getAccuracy());
        System.out.printf("  총 시도:  %d회%n", store.getTotalAttempts());
        System.out.printf("  총 정답:  %d회%n", store.getTotalCorrect());
        System.out.printf("  오답노트: %d문제%n", store.getWrongNotes().size());

        // 카테고리별 오답 현황
        if (!store.getWrongIds().isEmpty()) {
            System.out.println();
            System.out.println("  ⚠️  미해결 오답: " + String.join(", ", store.getWrongIds()));
        }
    }

    // ─── 개념 정리 메뉴 ─────────────────────────────────────────

    private void showConceptMenu() {
        while (true) {
            conceptViewer.printCategoryMenu();
            System.out.println("  b) 메인 메뉴로");
            System.out.print("선택 > ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("b")) return;

            if (input.equalsIgnoreCase("a")) {
                conceptViewer.printAll();
                System.out.print("\n엔터를 눌러 메뉴로...");
                scanner.nextLine();
                continue;
            }

            try {
                List<String> categories = conceptViewer.getCategories();
                int idx = Integer.parseInt(input) - 1;
                if (idx < 0 || idx >= categories.size()) {
                    CliPrinter.error("잘못된 선택입니다.");
                    continue;
                }
                String selected = categories.get(idx);
                conceptViewer.printCategory(selected);

                // 해당 카테고리 문제 바로 풀기 여부
                System.out.print("\n이 카테고리 문제를 바로 풀까요? (y/n) > ");
                if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                    startQuiz(engine.getByCategory(selected));
                }
            } catch (NumberFormatException e) {
                CliPrinter.error("숫자 또는 a/b를 입력해주세요.");
            }
        }
    }
}
