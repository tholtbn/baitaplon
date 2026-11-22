package com.auction.server.network;

import com.auction.server.dispatch.CommandDispatcher;
import com.auction.server.error.ExceptionMapper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private final int port;
    private final SessionRegistry sessionRegistry;
    private final CommandDispatcher dispatcher;
    private final ExceptionMapper exceptionMapper;
    private final ExecutorService threadPool;

    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public SocketServer(int port, SessionRegistry sessionRegistry) {
        this(port, sessionRegistry, null, null);
    }

    public SocketServer(
            int port,
            SessionRegistry sessionRegistry,
            CommandDispatcher dispatcher,
            ExceptionMapper exceptionMapper
    ) {
        this.port = port;
        this.sessionRegistry = sessionRegistry;
        this.dispatcher = dispatcher;
        this.exceptionMapper = exceptionMapper;
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void start() throws Exception {
        serverSocket = new ServerSocket(port);
        running = true;
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientSession session = createSession(clientSocket);
                threadPool.submit(session);
            } catch (SocketException e) {
                if (!running) {
                    break;
                }
                throw e;
            }
        }
    }

    protected ClientSession createSession(Socket clientSocket) {
        ClientSession session = new ClientSession(clientSocket, sessionRegistry);
        return session;
    }

    public int getBoundPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : port;
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }
        threadPool.shutdown();
    }
}