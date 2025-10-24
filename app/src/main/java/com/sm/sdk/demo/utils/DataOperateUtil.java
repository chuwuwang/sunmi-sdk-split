package com.sm.sdk.demo.utils;


import java.util.Arrays;

public final class DataOperateUtil {
    private DataOperateUtil() {
        throw new AssertionError("create instance of DataOperateUtil is prohibited");
    }

    /**
     * left rotate a byte array
     *
     * @param src   the byte array to be rotated
     * @param shift left shit count
     * @return the rotated byte array
     */
    public static byte[] leftRotation(byte[] src, int shift) {
        if (src == null || src.length == 0 || shift < 0) {
            return null;
        }
        shift = shift % src.length;
        if (shift == 0) {
            return Arrays.copyOf(src, src.length);
        }
        byte[] result = new byte[src.length];
        byte[] tmp = Arrays.copyOf(src, shift);
        System.arraycopy(src, shift, result, 0, src.length - shift);
        System.arraycopy(tmp, 0, result, src.length - shift, shift);
        return result;
    }

    /**
     * right rotate a byte array
     *
     * @param src   the byte array to be rotated
     * @param shift right shit count
     * @return the rotated byte array
     */
    public static byte[] rightRotation(byte[] src, int shift) {
        if (src == null || src.length == 0 || shift < 0) {
            return null;
        }
        shift = shift % src.length;
        if (shift == 0) {
            return Arrays.copyOf(src, src.length);
        }
        byte[] result = new byte[src.length];
        byte[] tmp = Arrays.copyOfRange(src, src.length - shift, src.length);
        System.arraycopy(src, 0, result, shift, src.length - shift);
        System.arraycopy(tmp, 0, result, 0, shift);
        return result;
    }

    /**
     * XOR operation
     *
     * @param x input array 1
     * @param y input array 2
     * @return the XOR result array
     */
    public static byte[] xor(byte[] x, byte[] y) {
        if (x == null || y == null || x.length != y.length) {
            return null;
        }
        byte[] result = new byte[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = (byte) (x[i] ^ y[i]);
        }
        return result;
    }
}
