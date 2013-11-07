package com.timgroup.io;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class ReconnectingSocketOutputStream extends OutputStream {

    public static final int DEFAULT_RETRY_COUNT = 3;

    private final String host;
    private final int port;
    private final int retryCount;
    private SocketChannel channel;
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
        if (channel != null) {
            throw new IllegalStateException("already connected to " + channel);
        }

        SocketChannel channel = SocketChannel.open(new InetSocketAddress(host, port));
        Socket socket = channel.socket();
        socket.setKeepAlive(true); // might help
        socket.setTcpNoDelay(true);
        OutputStream out = socket.getOutputStream();

        // do this atomicallyish, so we can never have socket not null but out null
        this.channel = channel;
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] {(byte) b});
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = 0; i < retryCount; ++i) {
            if (channel == null || (!channel.isConnected()) || !channel.isOpen()) {
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
        if (channel == null) {
            return;
        }
        try {
            channel.close();
        } finally {
            channel = null;
            out = null;
        }
    }

}
