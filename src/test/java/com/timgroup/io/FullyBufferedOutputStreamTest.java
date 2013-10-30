package com.timgroup.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FullyBufferedOutputStreamTest {

    private static final int ONE_THOUSAND = 1024;

    @Test
    public void does_not_write_any_bytes_if_not_flushed() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        FullyBufferedOutputStream out = new FullyBufferedOutputStream(buf);

        byte[] k = new byte[ONE_THOUSAND];
        for (int i = 0; i < ONE_THOUSAND; ++i) {
            out.write(k);
        }

        assertEquals(0, buf.size());
    }

    @Test
    public void writes_all_bytes_if_flushed() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        FullyBufferedOutputStream out = new FullyBufferedOutputStream(buf);

        byte[] k = new byte[ONE_THOUSAND];
        for (int i = 0; i < ONE_THOUSAND; ++i) {
            out.write(k);
        }
        out.flush();

        assertEquals(ONE_THOUSAND * ONE_THOUSAND, buf.size());
    }

    @Test
    public void writes_all_bytes_if_closed() throws Exception {
        final AtomicBoolean closed = new AtomicBoolean();
        ByteArrayOutputStream buf = new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                closed.set(true);
            }
        };

        FullyBufferedOutputStream out = new FullyBufferedOutputStream(buf);

        byte[] k = new byte[ONE_THOUSAND];
        for (int i = 0; i < ONE_THOUSAND; ++i) {
            out.write(k);
        }
        out.close();

        assertEquals(ONE_THOUSAND * ONE_THOUSAND, buf.size());
        assertTrue(closed.get());
    }

}
