package com.linuxmaster.jeongbo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class McqProgressStore {

    private static final String DATA_DIR  = System.getProperty("user.home") + "/.linux-master";
    private static final String PROG_FILE = DATA_DIR + "/jeongbo_progress.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private ProgressData data;

    public McqProgressStore() {
        new File(DATA_DIR).mkdirs();
        load();
    }

    public void recordResult(McqQuestion question, int userAnswer) {
        data.totalAttempts++;
        boolean correct = (userAnswer == question.answer);

        if (correct) {
            data.totalCorrect++;
            data.solvedIds.add(question.id);
            data.wrongIds.remove(question.id);
            data.wrongNotes.removeIf(n -> n.questionId.equals(question.id));
        } else {
            data.wrongIds.add(question.id);
            WrongNote note = new WrongNote();
            note.questionId   = question.id;
            note.subject      = question.subject;
            note.question     = question.question;
            note.options      = question.options;
            note.userAnswer   = userAnswer;
            note.correctAnswer = question.answer;
            note.explanation  = question.explanation;
            note.timestamp    = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            data.wrongNotes.removeIf(n -> n.questionId.equals(question.id));
            data.wrongNotes.add(note);
        }
        save();
    }

    public Set<String> getSolvedIds()       { return data.solvedIds; }
    public Set<String> getWrongIds()        { return data.wrongIds; }
    public List<WrongNote> getWrongNotes()  { return data.wrongNotes; }
    public int getTotalAttempts()           { return data.totalAttempts; }
    public int getTotalCorrect()            { return data.totalCorrect; }

    public double getAccuracy() {
        if (data.totalAttempts == 0) return 0;
        return (double) data.totalCorrect / data.totalAttempts * 100;
    }

    private void load() {
        try {
            Path path = Paths.get(PROG_FILE);
            if (Files.exists(path)) {
                data = gson.fromJson(Files.readString(path), ProgressData.class);
            }
        } catch (Exception ignored) {}
        if (data == null) data = new ProgressData();
    }

    private void save() {
        try {
            Files.writeString(Paths.get(PROG_FILE), gson.toJson(data));
        } catch (IOException e) {
            System.err.println("진도 저장 실패: " + e.getMessage());
        }
    }

    public static class ProgressData {
        public int totalAttempts = 0;
        public int totalCorrect  = 0;
        public Set<String>   solvedIds  = new HashSet<>();
        public Set<String>   wrongIds   = new HashSet<>();
        public List<WrongNote> wrongNotes = new ArrayList<>();
    }

    public static class WrongNote {
        public String questionId;
        public String subject;
        public String question;
        public List<String> options;
        public int userAnswer;
        public int correctAnswer;
        public String explanation;
        public String timestamp;
    }
}
