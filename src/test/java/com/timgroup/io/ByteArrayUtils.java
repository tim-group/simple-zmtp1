package com.timgroup.io;

import java.util.Arrays;

public class ByteArrayUtils {

    public static byte[] pad(byte[] b, int length) {
        if (b.length >= length) return b;
        byte[] padding = new byte[length - b.length];
        return ByteArrayUtils.concatenate(b, padding);
    }

    public static byte[] concatenate(byte[]... parts) {
        int length = 0;
        for (byte[] part : parts) {
            length += part.length;
        }
        byte[] whole = new byte[length];
        int off = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, whole, off, part.length);
            off += part.length;
        }
        return whole;
    }

    public static byte[] fill(int length, byte val) {
        byte[] a = new byte[length];
        Arrays.fill(a, val);
        return a;
    }

}
