package ru.spbau.mit;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class QuizGame implements Game {

    private GameServer server;

    private int delayUntilNextLetter;
    private int maxLettersToOpen;
    private String dictionaryFilename;
    private Thread tick;

    private class Tick extends Thread {

        private int openedLetters;

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                questionAnswer.nextQuestion();
                server.broadcast(questionAnswer.getQuestion());

                openedLetters = 0;

                while (openedLetters < maxLettersToOpen) {
                    try {
                        Thread.sleep(delayUntilNextLetter);
                    } catch (InterruptedException e) {
                        return;
                    }
                    openedLetters++;
                    server.broadcast(getPrefixMessage());
                }

                if (!Thread.interrupted()) {
                    try {
                        Thread.sleep(delayUntilNextLetter);
                    } catch (InterruptedException e) {
                        return;
                    }
                    server.broadcast(getTimeoutMessage());
                }
            }
        }

        private String getPrefixMessage() {
            return "Current prefix is" + " "
                    + questionAnswer.getAnswer().substring(0, openedLetters);
        }

        private String getTimeoutMessage() {
            return "Nobody guessed, the word was" + " "
                    + questionAnswer.getAnswer();
        }
    }

    private static class QuestionAnswer {

        private String[] questions;
        private String[] answers;
        private int current = -1;

        QuestionAnswer(String dictionaryFilename) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(dictionaryFilename));

                List<String> questions = new LinkedList<>();
                List<String> answers = new LinkedList<>();

                while (reader.ready()) {
                    String str = reader.readLine();

                    int mid = str.indexOf(";");
                    String question = str.substring(0, mid);
                    String answer = str.substring(mid + 1);

                    questions.add(question);
                    answers.add(answer);
                }

                this.questions = questions.toArray(new String[questions.size()]);
                this.answers = answers.toArray(new String[answers.size()]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void nextQuestion() {
            current = (current + 1) % questions.length;
        }

        public String getQuestion() {
            return "New round started:" + " "
                    + questions[current] + " "
                    + "(" + Integer.toString(answers[current].length()) + " " + "letters" + ")";
        }

        public String getAnswer() {
            return answers[current];
        }
    }

    private QuestionAnswer questionAnswer;

    public QuizGame(GameServer server) {
        this.server = server;
    }

    @Override
    public void onPlayerConnected(String id) {
        System.out.println("Player " + id + " connected");
    }

    @Override
    public synchronized void onPlayerSentMsg(String id, String msg) {
        System.out.println("Player " + id + ": " + msg);
        switch (msg) {
            case "!start":
                tick = new Tick();
                tick.start();
                break;
            case "!stop":
                while (!tick.isInterrupted()) {
                    tick.interrupt();
                }
                server.broadcast(getStopMessage(id));
                break;
            default:
                if (questionAnswer.getAnswer().equals(msg)) {
                    tick.interrupt();
                    server.broadcast(getWinMessage(id));
                    tick = new Tick();
                    tick.start();
                } else {
                    server.sendTo(id, "Wrong try");
                }
        }
    }

    private String getStopMessage(String id) {
        return "Game has been stopped by" + " " + id;
    }

    private String getWinMessage(String id) {
        return "The winner is " + id;
    }

    public void setDictionaryFilename(String dictionaryFilename) {
        this.dictionaryFilename = dictionaryFilename;
        questionAnswer = new QuestionAnswer(dictionaryFilename);
    }

    public void setDelayUntilNextLetter(int delayUntilNextLetter) {
        this.delayUntilNextLetter = delayUntilNextLetter;
    }

    public void setMaxLettersToOpen(int maxLettersToOpen) {
        this.maxLettersToOpen = maxLettersToOpen;
    }
}
