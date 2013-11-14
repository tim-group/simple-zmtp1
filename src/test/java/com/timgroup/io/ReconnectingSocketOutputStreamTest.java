package com.timgroup.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ReconnectingSocketOutputStreamTest {

    private static final int MINIMUM_DATA_TO_GET_BROKEN_PIPE = 160 * 1024 + 1;
    private static final Random RANDOM = new Random();

    @Rule
    public final TestRule timeout = new Timeout(1000);

    private final int port = 1024 + RANDOM.nextInt(65536 - 1024);
    private ServerSocket serverSocket;

    @Before
    public void closeServerSocket() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    @Test
    public void reportsFailureToConnect() throws Exception {
        try {
            new ReconnectingSocketOutputStream("localhost", port);
            fail("should have thrown ConnectException");
        } catch (ConnectException e) {
            // expected
        }
    }

    @Test
    public void writesBytesToASocket() throws Exception {
        serverSocket = new ServerSocket(port);

        Future<String> line = readLine(serverSocket);

        ReconnectingSocketOutputStream out = new ReconnectingSocketOutputStream("localhost", port);
        out.write("hello\n".getBytes());
        out.close();

        assertEquals("hello", line.get(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void reconnectsIfNecessaryOnWrite() throws Exception {
        serverSocket = new ServerSocket(port);

        Future<Socket> socket = connect(serverSocket);

        ReconnectingSocketOutputStream out = new ReconnectingSocketOutputStream("localhost", port);

        socket.get(100, TimeUnit.MILLISECONDS).close();
        Future<String> line = readLine(serverSocket);

        out.write(ByteArrayUtils.pad("hello\n".getBytes(), MINIMUM_DATA_TO_GET_BROKEN_PIPE));
        out.close();

        assertEquals("hello", line.get(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void blocksIfNecessaryOnWrite() throws Exception {
        serverSocket = new ServerSocket(port);
        byte[] stuffing = ByteArrayUtils.fill(1024, (byte) '\n');

        Future<String> line = eventuallyReadFirstNonBlankLine(serverSocket, 500);

        ReconnectingSocketOutputStream out = new ReconnectingSocketOutputStream("localhost", port);

        for (int i = 0; i < 2 * MINIMUM_DATA_TO_GET_BROKEN_PIPE / stuffing.length; ++i) {
            out.write(stuffing);
        }
        out.write("hello\n".getBytes());
        out.close();

        assertEquals("hello", line.get(700, TimeUnit.MILLISECONDS));
    }

    private Future<String> readLine(final ServerSocket serverSocket) {
        return Executors.newSingleThreadExecutor().submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Socket socket = serverSocket.accept();
                try {
                    return new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
                } finally {
                    socket.close();
                }
            }
        });
    }

    private Future<Socket> connect(final ServerSocket serverSocket) {
        return Executors.newSingleThreadExecutor().submit(new Callable<Socket>() {
            @Override
            public Socket call() throws Exception {
                return serverSocket.accept();
            }
        });
    }

    private Future<String> eventuallyReadFirstNonBlankLine(final ServerSocket serverSocket, final int delay) {
        return Executors.newSingleThreadExecutor().submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Socket socket = serverSocket.accept();
                Thread.sleep(delay);
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null && line.equals(""));
                    return line;
                } finally {
                    socket.close();
                }
            }
        });
    }

}
