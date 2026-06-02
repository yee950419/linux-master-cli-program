package com.linuxmaster.concept;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.linuxmaster.ui.CliPrinter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class ConceptViewer {

    private List<ConceptNote> notes = new ArrayList<>();
    private final Gson gson = new Gson();

    public void load(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) { System.err.println("개념 파일 없음: " + resourcePath); return; }
            Type type = new TypeToken<List<ConceptNote>>(){}.getType();
            notes = gson.fromJson(new InputStreamReader(is), type);
        } catch (Exception e) {
            System.err.println("개념 로드 실패: " + e.getMessage());
        }
    }

    public List<String> getCategories() {
        return notes.stream().map(n -> n.category).distinct().collect(Collectors.toList());
    }

    public List<ConceptNote> getByCategory(String category) {
        return notes.stream().filter(n -> n.category.equals(category)).collect(Collectors.toList());
    }

    public Optional<ConceptNote> getById(String id) {
        return notes.stream().filter(n -> n.id.equals(id)).findFirst();
    }

    // ─── 전체 카테고리 목록 출력 ────────────────────────────────

    public void printCategoryMenu() {
        List<String> categories = getCategories();
        CliPrinter.header("개념 정리 - 카테고리 선택");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("  %d) %s%n", i + 1, categories.get(i));
        }
        System.out.println("  a) 전체 개념 보기");
    }

    // ─── 단일 개념 노트 출력 ────────────────────────────────────

    public void printNote(ConceptNote note) {
        System.out.println();
        CliPrinter.header("📚 " + note.title);

        // 요약
        System.out.println("  " + note.summary);
        System.out.println();

        // 섹션별 출력
        for (ConceptNote.Section section : note.sections) {
            printSection(section.heading, section.content);
        }

        // 관련 문제
        if (note.relatedQuestionIds != null && !note.relatedQuestionIds.isEmpty()) {
            CliPrinter.separator();
            System.out.println("  🔗 관련 문제 ID: " + String.join(", ", note.relatedQuestionIds));
        }
    }

    // ─── 카테고리 전체 출력 ─────────────────────────────────────

    public void printCategory(String category) {
        List<ConceptNote> categoryNotes = getByCategory(category);
        if (categoryNotes.isEmpty()) {
            CliPrinter.error("해당 카테고리의 개념이 없습니다.");
            return;
        }
        CliPrinter.header("📖 " + category + " 개념 정리");
        for (ConceptNote note : categoryNotes) {
            printNote(note);
            System.out.println();
        }
    }

    public void printAll() {
        for (String category : getCategories()) {
            printCategory(category);
        }
    }

    // ─── 섹션 포맷 출력 ─────────────────────────────────────────

    private void printSection(String heading, String content) {
        // 헤딩
        System.out.println("\033[1m\033[36m  ▸ " + heading + "\033[0m");
        CliPrinter.separator();

        // 내용: 각 줄 들여쓰기 + 코드 형식 강조
        for (String line : content.split("\n")) {
            if (line.trim().isEmpty()) {
                System.out.println();
                continue;
            }
            // 명령어처럼 보이는 줄 (: 포함하거나 공백 없이 시작하는 경우) 강조
            if (line.contains(":") || line.trim().startsWith("-") || line.trim().startsWith("/")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    System.out.printf("\033[33m    %-35s\033[0m : %s%n",
                        parts[0].trim(), parts[1].trim());
                } else {
                    System.out.println("    " + line);
                }
            } else {
                System.out.println("    \033[90m" + line + "\033[0m");
            }
        }
        System.out.println();
    }

    public int totalCount() {
        return notes.size();
    }
}
