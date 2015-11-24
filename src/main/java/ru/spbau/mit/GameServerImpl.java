package ru.spbau.mit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


public class GameServerImpl implements GameServer {

    private final Game game;
    private int connectionsCounter = 0;
    private final Map<String, HandlerConnection> handlerConnectionMap =
            Collections.synchronizedMap(new Hashtable<String, HandlerConnection>());

    private class HandlerConnection extends Thread {

        private final String id;
        private final Connection connection;
        private final Queue<String> sendMsgQueue = new LinkedList<>();
        private final int TIMEOUT = 10;

        HandlerConnection(String id, Connection connection) {
            this.id = id;
            this.connection = connection;
            sendMessage(id);
        }

        @Override
        public void run() {
            try {
                game.onPlayerConnected(id);
                while (!connection.isClosed()) {
                    doSendTask();
                    doRecieveTask();
                }
            } catch (InterruptedException ignored) {
                connection.close();
            }
        }

        private void doSendTask() {
            synchronized (sendMsgQueue) {
                if (!connection.isClosed()) {
                    if (!sendMsgQueue.isEmpty()) {
                        String msg = sendMsgQueue.remove();
                        connection.send(msg);
                    }
                }
            }
        }

        private void doRecieveTask() throws InterruptedException {
            if (!connection.isClosed()) {
                String msg = connection.receive(TIMEOUT);
                if (msg != null) {
                    game.onPlayerSentMsg(id, msg);
                }
            }
        }

        public void sendMessage(String message) {
            synchronized (sendMsgQueue) {
                sendMsgQueue.add(message);
            }
        }
    }

    public GameServerImpl(String gameClassName, Properties properties) throws ClassNotFoundException,
                                                                            NoSuchMethodException,
                                                                            IllegalAccessException,
                                                                            InvocationTargetException,
                                                                            InstantiationException {

        Class<?> aClass = Class.forName(gameClassName);
        Constructor<?> constructor = aClass.getConstructor(GameServer.class);
        game = (Game) constructor.newInstance(this);

        for (String key : properties.stringPropertyNames()) {
            String methodName = getSetterName(key);
            String arg = properties.getProperty(key);

            if (isInt(arg)) {
                Method setter = aClass.getMethod(methodName, int.class);
                setter.invoke(game, Integer.parseInt(arg));
            } else {
                Method setter = aClass.getMethod(methodName, String.class);
                setter.invoke(game, arg);
            }
        }
    }

    private String getSetterName(String key) {
        return "set" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
    }

    private boolean isInt(String val) {
        for (char c : val.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public synchronized void accept(final Connection connection) {
        String id = Integer.toString(connectionsCounter++);
        HandlerConnection handlerConnection = new HandlerConnection(id, connection);
        handlerConnectionMap.put(id, handlerConnection);
        handlerConnection.start();
    }

    @Override
    public void broadcast(String message) {
        for (int i = 0; i < connectionsCounter; i++) {
            sendTo(Integer.toString(i), message);
        }
    }

    @Override
    public void sendTo(String id, String message) {
        HandlerConnection handlerConnection = handlerConnectionMap.get(id);
        handlerConnection.sendMessage(message);
    }
}
