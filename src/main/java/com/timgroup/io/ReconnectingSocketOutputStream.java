package com.timgroup.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class ReconnectingSocketOutputStream extends OutputStream {

    public static final int DEFAULT_TRY_COUNT = 3;

    private final String host;
    private final int port;
    private final int tryCount;
    private final Selector selector;
    private final ByteBuffer drainBuffer;
    private SocketChannel channel;

    public ReconnectingSocketOutputStream(String host, int port, int tryCount) throws IOException {
        if (tryCount < 1) throw new IllegalArgumentException("tryCount must be at least one");
        this.host = host;
        this.port = port;
        this.tryCount = tryCount;
        this.selector = Selector.open();
        this.drainBuffer = ByteBuffer.allocate(32); // this is small because for ZMTP, we do not expect much data to come our way
        connect();
    }

    public ReconnectingSocketOutputStream(String host, int port) throws IOException {
        this(host, port, DEFAULT_TRY_COUNT);
    }

    private void connect() throws IOException {
        if (channel != null) {
            throw new IllegalStateException("already connected to " + channel);
        }

        SocketChannel channel = SocketChannel.open(new InetSocketAddress(host, port));
        Socket socket = channel.socket();
        socket.setKeepAlive(true); // might help
        socket.setTcpNoDelay(true);
        channel.configureBlocking(false);

        this.channel = channel;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] {(byte) b});
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        List<IOException> exceptions = null;
        for (int i = 0; i < tryCount; ++i) {
            try {
                ensureOpen();
                ByteBuffer buffer = ByteBuffer.wrap(b, off, len);
                while (buffer.hasRemaining()) {
                    checkForRead();
                    int written = channel.write(buffer);
                    if (written == 0) {
                        blockForWrite();
                    } else if (written < 0) {
                        throw new EOFException();
                    }
                }
                return;
            } catch (IOException e) {
                if (exceptions == null) {
                    exceptions = new ArrayList<IOException>(tryCount);
                }
                exceptions.add(e);
                closeQuietly();
            }
        }
        assert exceptions != null && !exceptions.isEmpty();
        throw new IOException("write failed after " + tryCount + " tries; exceptions = " + exceptions, exceptions.get(0));
    }

    private void ensureOpen() throws IOException {
        if (channel == null || !channel.isOpen()) {
            try {
                reconnect();
            } catch (ConnectException e) {
                sleep(1000);
                throw e;
            }
        }
    }

    private void sleep(int millis) throws InterruptedIOException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            InterruptedIOException e2 = new InterruptedIOException();
            e2.initCause(e);
            throw e2;
        }
    }

    private void checkForRead() throws IOException {
        SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
        selector.selectNow();
        if (key.isReadable()) {
            drain();
        }
    }

    private void blockForWrite() throws IOException {
        SelectionKey key = channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        while (!key.isWritable()) {
            int selected = selector.select();
            if (selected == 0) {
                throw new IOException("failed to select");
            }
            if (key.isReadable()) {
                drain();
            }
        }
    }

    private void drain() throws IOException, EOFException {
        int read;
        while ((read = channel.read(drainBuffer)) > 0);
        if (read < 0) throw new EOFException();
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
