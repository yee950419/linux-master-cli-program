package com.linuxmaster.jeongbo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class McqEngine {

    private List<McqQuestion> allQuestions = new ArrayList<>();
    private final Gson gson = new Gson();

    public void loadQuestions(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("문제 파일을 찾을 수 없습니다: " + resourcePath);
                return;
            }
            Type listType = new TypeToken<List<McqQuestion>>(){}.getType();
            allQuestions = gson.fromJson(new InputStreamReader(is), listType);
        } catch (Exception e) {
            System.err.println("문제 로드 실패: " + e.getMessage());
        }
    }

    public void loadAllYears(int[] years) {
        allQuestions = new ArrayList<>();
        for (int year : years) {
            String path = "/questions/jeongbo_기출_" + year + ".json";
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is == null) continue;
                Type listType = new TypeToken<List<McqQuestion>>(){}.getType();
                List<McqQuestion> loaded = gson.fromJson(new InputStreamReader(is), listType);
                allQuestions.addAll(loaded);
            } catch (Exception e) {
                System.err.println(year + "년 문제 로드 실패: " + e.getMessage());
            }
        }
    }

    public List<McqQuestion> getAll() {
        return Collections.unmodifiableList(allQuestions);
    }

    public List<McqQuestion> getBySubject(String subject) {
        return allQuestions.stream()
            .filter(q -> q.subject.equals(subject))
            .collect(Collectors.toList());
    }

    public List<McqQuestion> getWrongQuestions(Set<String> wrongIds) {
        return allQuestions.stream()
            .filter(q -> wrongIds.contains(q.id))
            .collect(Collectors.toList());
    }

    public List<McqQuestion> getRandomSet(int count) {
        List<McqQuestion> shuffled = new ArrayList<>(allQuestions);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    public List<McqQuestion> getByYear(int year) {
        return allQuestions.stream()
            .filter(q -> q.year == year)
            .collect(Collectors.toList());
    }

    public List<McqQuestion> getByYearRound(int year, int round) {
        return allQuestions.stream()
            .filter(q -> q.year == year && q.round == round)
            .collect(Collectors.toList());
    }

    public List<Integer> getAvailableYears() {
        return allQuestions.stream()
            .map(q -> q.year)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    public Set<String> getSubjects() {
        Set<String> subjects = new LinkedHashSet<>();
        allQuestions.forEach(q -> subjects.add(q.subject));
        return subjects;
    }

    public int totalCount() {
        return allQuestions.size();
    }
}
