package com.linuxmaster.quiz;

public class QuizResult {

    public enum Grade { CORRECT, PARTIAL, WRONG }

    public final QuizQuestion question;
    public final String userAnswer;
    public final String actualOutput;
    public final Grade grade;
    public final String feedback;

    public QuizResult(QuizQuestion question, String userAnswer,
                      String actualOutput, Grade grade, String feedback) {
        this.question = question;
        this.userAnswer = userAnswer;
        this.actualOutput = actualOutput;
        this.grade = grade;
        this.feedback = feedback;
    }

    public boolean isCorrect()  { return grade == Grade.CORRECT; }
    public boolean isPartial()  { return grade == Grade.PARTIAL; }
    public boolean isWrong()    { return grade == Grade.WRONG; }
}
