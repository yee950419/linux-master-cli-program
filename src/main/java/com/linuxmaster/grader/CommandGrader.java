package com.linuxmaster.grader;

import com.linuxmaster.quiz.QuizQuestion;
import com.linuxmaster.quiz.QuizResult;
import com.linuxmaster.quiz.QuizResult.Grade;
import com.linuxmaster.sandbox.DockerSandbox;
import com.linuxmaster.sandbox.DockerSandbox.ExecutionResult;

public class CommandGrader {

    private final DockerSandbox sandbox;

    public CommandGrader(DockerSandbox sandbox) {
        this.sandbox = sandbox;
    }

    /**
     * 문제 환경 세팅 (setupCommand 실행)
     */
    public void setup(QuizQuestion question) {
        if (question.setupCommand != null && !question.setupCommand.isBlank()) {
            sandbox.execute(question.setupCommand);
        }
    }

    /**
     * 사용자 명령어를 실행하고 채점
     */
    public QuizResult grade(QuizQuestion question, String userCommand) {

        // 1. 사용자 명령어 실행
        ExecutionResult userResult = sandbox.execute(userCommand);

        if (userResult.status == ExecutionResult.Status.BLOCKED) {
            return new QuizResult(question, userCommand, userResult.output,
                Grade.WRONG, "🚫 차단된 명령어입니다: " + userResult.output);
        }
        if (userResult.status == ExecutionResult.Status.TIMEOUT) {
            return new QuizResult(question, userCommand, userResult.output,
                Grade.WRONG, "⏱️  실행 시간 초과 (10초)");
        }
        if (userResult.status == ExecutionResult.Status.ERROR) {
            return new QuizResult(question, userCommand, userResult.output,
                Grade.WRONG, "⚠️  실행 오류: " + userResult.output);
        }

        // 2. 채점 방식 분기
        if (question.useOutputGrading) {
            return gradeByOutput(question, userCommand, userResult.output);
        } else {
            return gradeByCommand(question, userCommand, userResult.output);
        }
    }

    // ─── 출력 비교 채점 ───────────────────────────────────────────────

    /**
     * 실행 결과로 채점.
     * gradingCommand가 있으면 그 명령어의 출력을 expectedOutput과 비교.
     * __DYNAMIC__ 이면 정답 명령어를 실행해 기준 출력을 만들고 비교.
     */
    private QuizResult gradeByOutput(QuizQuestion question, String userCommand, String actualOutput) {

        String expected = question.expectedOutput;

        // 동적 출력 채점: 모범 명령어 실행 결과와 비교
        if ("__DYNAMIC__".equals(expected)) {
            if (question.answers == null || question.answers.isEmpty()) {
                return new QuizResult(question, userCommand, actualOutput,
                    Grade.WRONG, "채점 오류: 모범 답안이 없습니다.");
            }
            ExecutionResult modelResult = sandbox.execute(question.answers.get(0));
            expected = modelResult.output;
        }

        // gradingCommand가 있으면 별도 명령어 결과로 채점
        String gradeTarget = actualOutput;
        if (question.gradingCommand != null && !question.gradingCommand.isBlank()) {
            ExecutionResult gradeResult = sandbox.execute(question.gradingCommand);
            gradeTarget = gradeResult.output;
        }

        String trimmedExpected = expected.replace("\\n", "\n").trim();
        String trimmedActual   = gradeTarget.trim();

        if (trimmedActual.equals(trimmedExpected)) {
            return new QuizResult(question, userCommand, actualOutput,
                Grade.CORRECT, "✅ 정답! 실행 결과가 일치합니다.");
        }

        // 핵심 첫 줄 일치 시 부분 점수
        String firstExpected = trimmedExpected.split("\n")[0];
        if (!firstExpected.isEmpty() && trimmedActual.contains(firstExpected)) {
            return new QuizResult(question, userCommand, actualOutput,
                Grade.PARTIAL, "⚠️  부분 정답: 출력 결과가 일부만 일치합니다.");
        }

        return new QuizResult(question, userCommand, actualOutput,
            Grade.WRONG,
            "❌ 오답\n  예상 출력: " + trimmedExpected.replace("\n", " / ") +
            "\n  실제 출력: " + trimmedActual.replace("\n", " / "));
    }

    // ─── 명령어 문자열 채점 ──────────────────────────────────────────

    private QuizResult gradeByCommand(QuizQuestion question, String userCommand, String actualOutput) {
        String normalizedUser = normalize(userCommand);

        for (String answer : question.answers) {
            if (normalize(answer).equals(normalizedUser)) {
                return new QuizResult(question, userCommand, actualOutput,
                    Grade.CORRECT, "✅ 정답!");
            }
        }

        // 기본 명령어(첫 토큰)가 맞는 경우 부분 점수
        String baseCmd = userCommand.trim().split("\\s+")[0];
        for (String answer : question.answers) {
            if (answer.trim().startsWith(baseCmd)) {
                return new QuizResult(question, userCommand, actualOutput,
                    Grade.PARTIAL, "⚠️  부분 정답: 명령어는 맞지만 옵션이 다릅니다.");
            }
        }

        return new QuizResult(question, userCommand, actualOutput, Grade.WRONG, "❌ 오답");
    }

    private String normalize(String cmd) {
        return cmd.trim().replaceAll("\\s+", " ");
    }
}
