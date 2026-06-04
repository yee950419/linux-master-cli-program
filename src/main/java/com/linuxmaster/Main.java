package com.linuxmaster;

import com.linuxmaster.app.AppRunner;
import com.linuxmaster.jeongbo.JeongboRunner;
import com.linuxmaster.ui.CliPrinter;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            CliPrinter.header("📚 자격증 시험 준비 프로그램");
            System.out.println();
            CliPrinter.plain("  어떤 시험을 준비하시겠습니까?");
            System.out.println();
            CliPrinter.plain("  1) 리눅스 마스터 1급 (실기)");
            CliPrinter.plain("  2) 정보처리기사 (필기)");
            CliPrinter.plain("  q) 종료");
            CliPrinter.separator();
            System.out.print("선택 > ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> new AppRunner().run();
                case "2" -> new JeongboRunner().run();
                case "q", "Q" -> {
                    CliPrinter.info("\n👋 종료합니다. 수고하셨습니다!");
                    return;
                }
                default -> CliPrinter.error("1, 2 또는 q를 입력해주세요.");
            }
        }
    }
}
