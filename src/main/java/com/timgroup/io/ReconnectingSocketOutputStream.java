package com.timgroup.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ReconnectingSocketOutputStream extends OutputStream {

    public static final int DEFAULT_RETRY_COUNT = 3;

    private final String host;
    private final int port;
    private final int retryCount;
    private SocketChannel channel;

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

        this.channel = channel;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] {(byte) b});
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = 0; i < retryCount; ++i) {
            if (channel == null || !channel.isOpen()) {
                reconnect();
            }
            try {
                ByteBuffer buffer = ByteBuffer.wrap(b, off, len);
                while (buffer.hasRemaining()) {
                    int written = channel.write(buffer);
                    if (written < 0) {
                        throw new EOFException();
                    }
                }
                return;
            } catch (Exception e) {
                closeQuietly();
            }
        }
    }

    public void reconnect() throws IOException {
        closeQuietly();
        connect();
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
        }
    }

}
