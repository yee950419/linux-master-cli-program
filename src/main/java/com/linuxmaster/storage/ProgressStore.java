package com.linuxmaster.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.linuxmaster.quiz.QuizQuestion;
import com.linuxmaster.quiz.QuizResult;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ProgressStore {

    private static final String DATA_DIR = System.getProperty("user.home") + "/.linux-master";
    private static final String PROGRESS_FILE = DATA_DIR + "/progress.json";
    private static final String WRONG_NOTE_FILE = DATA_DIR + "/wrong_notes.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private ProgressData data;

    public ProgressStore() {
        new File(DATA_DIR).mkdirs();
        load();
    }

    // ─── 진도 기록 ────────────────────────────────────────────────

    public void recordResult(QuizResult result) {
        String questionId = result.question.id;
        data.totalAttempts++;

        if (result.isCorrect()) {
            data.totalCorrect++;
            data.solvedIds.add(questionId);
            data.wrongIds.remove(questionId);
        } else {
            data.wrongIds.add(questionId);
            // 오답노트에 추가
            WrongNote note = new WrongNote();
            note.questionId = questionId;
            note.question = result.question.question;
            note.userAnswer = result.userAnswer;
            note.correctAnswer = result.question.answers.isEmpty() ? "" : result.question.answers.get(0);
            note.explanation = result.question.explanation;
            note.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            data.wrongNotes.removeIf(n -> n.questionId.equals(questionId));
            data.wrongNotes.add(note);
        }
        save();
    }

    public Set<String> getSolvedIds()   { return data.solvedIds; }
    public Set<String> getWrongIds()    { return data.wrongIds; }
    public List<WrongNote> getWrongNotes() { return data.wrongNotes; }
    public int getTotalAttempts()       { return data.totalAttempts; }
    public int getTotalCorrect()        { return data.totalCorrect; }

    public double getAccuracy() {
        if (data.totalAttempts == 0) return 0;
        return (double) data.totalCorrect / data.totalAttempts * 100;
    }

    // ─── 파일 I/O ────────────────────────────────────────────────

    private void load() {
        try {
            Path path = Paths.get(PROGRESS_FILE);
            if (Files.exists(path)) {
                String json = Files.readString(path);
                data = gson.fromJson(json, ProgressData.class);
            }
        } catch (Exception ignored) {}
        if (data == null) data = new ProgressData();
    }

    private void save() {
        try {
            Files.writeString(Paths.get(PROGRESS_FILE), gson.toJson(data));
        } catch (IOException e) {
            System.err.println("진도 저장 실패: " + e.getMessage());
        }
    }

    // ─── 내부 모델 ────────────────────────────────────────────────

    public static class ProgressData {
        public int totalAttempts = 0;
        public int totalCorrect = 0;
        public Set<String> solvedIds = new HashSet<>();
        public Set<String> wrongIds = new HashSet<>();
        public List<WrongNote> wrongNotes = new ArrayList<>();
    }

    public static class WrongNote {
        public String questionId;
        public String question;
        public String userAnswer;
        public String correctAnswer;
        public String explanation;
        public String timestamp;
    }
}
