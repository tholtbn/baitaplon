package com.auction.server.network;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SocketServerTest {

    @Test
    void serverShouldStartOnEphemeralPortAndAcceptClientConnection() throws Exception {
        SessionRegistry registry = new SessionRegistry();
        SocketServer server = new SocketServer(0, registry);

        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception ignored) {
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Chờ server bind cổng thực tế
        int boundPort = -1;
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3);
        while (System.currentTimeMillis() < deadline) {
            if (server.getBoundPort() > 0) {
                boundPort = server.getBoundPort();
                break;
            }
            Thread.sleep(20);
        }
        assertTrue(boundPort > 0, "SocketServer phải bind được cổng > 0");

        try (Socket client = new Socket("127.0.0.1", boundPort);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

            assertTrue(client.isConnected());
            // Gửi một dòng request bất kỳ; hiện dispatcher đang null nên kỳ vọng phản hồi lỗi JSON có cấu trúc
            out.println("{\"type\":\"PING\"}");
            String line = in.readLine();
            assertNotNull(line);
            assertTrue(line.contains("ERROR"));
        } finally {
            server.stop();
        }
    }
}