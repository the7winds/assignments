package ru.spbau.mit;

import java.util.*;


public class SumTwoNumbersGame implements Game {

    private final GameServer server;

    private int a;
    private int b;
    private final Random rnd = new Random();

    public SumTwoNumbersGame(GameServer server) {
        this.server = server;
        genNumbers();
    }

    private void genNumbers() {
        a = Math.abs(rnd.nextInt());
        b = Math.abs(rnd.nextInt());
    }

    private String getGameStateMessage() {
        return Integer.toString(a) + " " + Integer.toString(b);
    }

    private String getWinMessage(String id) {
        return id + " won";
    }

    @Override
    public void onPlayerConnected(String id) {
        server.sendTo(id, getGameStateMessage());
    }

    @Override
    public synchronized void onPlayerSentMsg(String id, String msg) {
        if (Integer.parseInt(msg) == a + b) {
            server.sendTo(id, "Right");
            genNumbers();
            server.broadcast(getWinMessage(id));
            server.broadcast(getGameStateMessage());

        } else {
            server.sendTo(id, "Wrong");
        }
    }
}
