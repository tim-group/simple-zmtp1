package com.timgroup.io;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ReconnectingSocketOutputStream extends OutputStream {

    public static final int DEFAULT_RETRY_COUNT = 3;

    private final String host;
    private final int port;
    private final int retryCount;
    private Socket socket;
    private OutputStream out;

    public ReconnectingSocketOutputStream(String host, int port, int retryCount) throws IOException {
        this.host = host;
        this.port = port;
        this.retryCount = retryCount;
        connect();
    }

    public ReconnectingSocketOutputStream(String host, int port) throws IOException {
        this(host, port, DEFAULT_RETRY_COUNT);
    }

    private void connect() throws IOException {
        if (socket != null) {
            throw new IllegalStateException("already connected to " + socket);
        }

        Socket socket = new Socket(host, port);
        socket.shutdownInput();
        socket.setKeepAlive(true); // might help
        socket.setTcpNoDelay(true);
        OutputStream out = socket.getOutputStream();

        // do this atomicallyish, so we can never have socket not null but out null
        this.socket = socket;
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] {(byte) b});
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = 0; i < retryCount; ++i) {
            if (socket == null || (!socket.isConnected()) || socket.isClosed()) {
                closeQuietly();
                connect();
            }
            try {
                out.write(b, off, len);
                out.flush();
                return;
            } catch (Exception e) {
                closeQuietly();
            }
        }
    }

    public void closeQuietly() {
        try {
            close();
        } catch (IOException e) {}
    }

    @Override
    public void close() throws IOException {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } finally {
            socket = null;
            out = null;
        }
    }

}
