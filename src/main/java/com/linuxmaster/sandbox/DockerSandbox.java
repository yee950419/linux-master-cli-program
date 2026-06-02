package com.linuxmaster.sandbox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class DockerSandbox {

    private static final String CONTAINER_NAME = "linux-master";
    private static final int TIMEOUT_SECONDS = 10;

    public ExecutionResult execute(String command) {
        try {
            // 위험한 명령어 사전 차단
            if (isDangerous(command)) {
                return ExecutionResult.blocked("위험한 명령어가 감지되었습니다: " + command);
            }

            ProcessBuilder pb = new ProcessBuilder(
                "docker", "exec", CONTAINER_NAME,
                "bash", "-c", command
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return ExecutionResult.timeout("명령어 실행 시간 초과 (" + TIMEOUT_SECONDS + "초)");
            }

            int exitCode = process.exitValue();
            return ExecutionResult.success(output.toString().trim(), exitCode);

        } catch (Exception e) {
            return ExecutionResult.error("실행 오류: " + e.getMessage());
        }
    }

    /**
     * 컨테이너가 실행 중인지 확인
     */
    public boolean isContainerRunning() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "docker", "inspect", "-f", "{{.State.Running}}", CONTAINER_NAME
            );
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();
            process.waitFor(5, TimeUnit.SECONDS);
            return "true".equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 위험한 명령어 패턴 차단
     */
    private boolean isDangerous(String command) {
        String[] dangerousPatterns = {
            "rm -rf /",
            "dd if=/dev/zero",
            "mkfs",
            ":(){ :|:& };:",   // fork bomb
            "> /dev/sda",
            "chmod -R 777 /",
            "chown -R",
            "shutdown",
            "reboot",
            "halt",
            "poweroff"
        };
        String lower = command.toLowerCase().trim();
        for (String pattern : dangerousPatterns) {
            if (lower.contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // ─── 결과 모델 ────────────────────────────────────────────────

    public static class ExecutionResult {
        public enum Status { SUCCESS, BLOCKED, TIMEOUT, ERROR }

        public final Status status;
        public final String output;
        public final int exitCode;

        private ExecutionResult(Status status, String output, int exitCode) {
            this.status = status;
            this.output = output;
            this.exitCode = exitCode;
        }

        public static ExecutionResult success(String output, int exitCode) {
            return new ExecutionResult(Status.SUCCESS, output, exitCode);
        }
        public static ExecutionResult blocked(String msg) {
            return new ExecutionResult(Status.BLOCKED, msg, -1);
        }
        public static ExecutionResult timeout(String msg) {
            return new ExecutionResult(Status.TIMEOUT, msg, -1);
        }
        public static ExecutionResult error(String msg) {
            return new ExecutionResult(Status.ERROR, msg, -1);
        }

        public boolean isSuccess() { return status == Status.SUCCESS; }
    }
}
