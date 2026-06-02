package com.linuxmaster.ui;

public class CliPrinter {

    // ANSI 색상 코드
    private static final String RESET  = "\033[0m";
    private static final String BOLD   = "\033[1m";
    private static final String GREEN  = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String RED    = "\033[31m";
    private static final String CYAN   = "\033[36m";
    private static final String GRAY   = "\033[90m";
    private static final String WHITE  = "\033[97m";

    public static void header(String text) {
        System.out.println();
        System.out.println(BOLD + CYAN + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
        System.out.println(BOLD + WHITE + "  " + text + RESET);
        System.out.println(BOLD + CYAN + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
    }

    public static void question(int current, int total, String category, String difficulty, String text) {
        System.out.println();
        System.out.printf(BOLD + "[문제 %d/%d] " + RESET + CYAN + "카테고리: %s " + RESET + GRAY + "| 난이도: %s%n" + RESET,
                current, total, category, difficulty);
        System.out.println(BOLD + CYAN + "─────────────────────────────────────────────────" + RESET);
        System.out.println(BOLD + WHITE + text + RESET);
        System.out.println(BOLD + CYAN + "─────────────────────────────────────────────────" + RESET);
        System.out.println(GRAY + "  [h] 힌트 보기  [s] 건너뛰기  [q] 종료" + RESET);
    }

    public static void prompt() {
        System.out.print(BOLD + GREEN + "\n> " + RESET);
    }

    public static void correct(String message) {
        System.out.println(BOLD + GREEN + "\n" + message + RESET);
    }

    public static void partial(String message) {
        System.out.println(BOLD + YELLOW + "\n" + message + RESET);
    }

    public static void wrong(String message) {
        System.out.println(BOLD + RED + "\n" + message + RESET);
    }

    public static void info(String message) {
        System.out.println(CYAN + message + RESET);
    }

    public static void hint(String hintText) {
        System.out.println(YELLOW + "💡 힌트: " + hintText + RESET);
    }

    public static void explanation(String correctAnswer, String explanationText) {
        System.out.println();
        System.out.println(BOLD + "📖 해설" + RESET);
        System.out.println(GREEN + "  모범 답안: " + correctAnswer + RESET);
        System.out.println(GRAY  + "  " + explanationText + RESET);
    }

    public static void executionOutput(String output) {
        System.out.println(GRAY + "┌── 실행 결과 ─────────────────────────────────────" + RESET);
        if (output.isEmpty()) {
            System.out.println(GRAY + "│  (출력 없음)" + RESET);
        } else {
            for (String line : output.split("\n")) {
                System.out.println(GRAY + "│  " + RESET + line);
            }
        }
        System.out.println(GRAY + "└──────────────────────────────────────────────────" + RESET);
    }

    public static void stats(int solved, int total, double accuracy) {
        System.out.println();
        System.out.println(BOLD + "📊 현재 진도" + RESET);
        System.out.printf("  풀이 완료: %d / %d 문제%n", solved, total);
        System.out.printf("  정답률:    %.1f%%%n", accuracy);
    }

    public static void separator() {
        System.out.println(GRAY + "─────────────────────────────────────────────────" + RESET);
    }

    public static void error(String message) {
        System.out.println(RED + "⚠️  " + message + RESET);
    }

    public static void plain(String message) {
        System.out.println(message);
    }
}
