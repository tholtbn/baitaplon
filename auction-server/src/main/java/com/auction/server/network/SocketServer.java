package com.auction.server.network;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private final int port;
    private final SessionRegistry sessionRegistry;
    private final ExecutorService threadPool;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public SocketServer(int port, SessionRegistry sessionRegistry) {
        this.port = port;
        this.sessionRegistry = sessionRegistry;
        this.threadPool = Executors.newCachedThreadPool(); // Hoặc Virtual Threads Java 21
    }

    public void start() throws Exception {
        serverSocket = new ServerSocket(port);
        running = true;
        while (running) {
            Socket clientSocket = serverSocket.accept();
            ClientSession session = new ClientSession(clientSocket, sessionRegistry);
            threadPool.submit(session);
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (Exception ignored) {}
        threadPool.shutdown();
    }
}