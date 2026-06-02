package com.linuxmaster.quiz;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class QuizEngine {

    private List<QuizQuestion> allQuestions = new ArrayList<>();
    private final Gson gson = new Gson();

    public void loadQuestions(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("문제 파일을 찾을 수 없습니다: " + resourcePath);
                return;
            }
            Type listType = new TypeToken<List<QuizQuestion>>(){}.getType();
            allQuestions = gson.fromJson(new InputStreamReader(is), listType);
        } catch (Exception e) {
            System.err.println("문제 로드 실패: " + e.getMessage());
        }
    }

    public List<QuizQuestion> getAll() {
        return Collections.unmodifiableList(allQuestions);
    }

    public List<QuizQuestion> getByCategory(String category) {
        return allQuestions.stream()
            .filter(q -> q.category.equals(category))
            .collect(Collectors.toList());
    }

    public List<QuizQuestion> getWrongQuestions(Set<String> wrongIds) {
        return allQuestions.stream()
            .filter(q -> wrongIds.contains(q.id))
            .collect(Collectors.toList());
    }

    public List<QuizQuestion> getRandomSet(int count) {
        List<QuizQuestion> shuffled = new ArrayList<>(allQuestions);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    public Set<String> getCategories() {
        Set<String> categories = new LinkedHashSet<>();
        allQuestions.forEach(q -> categories.add(q.category));
        return categories;
    }

    public int totalCount() {
        return allQuestions.size();
    }
}
