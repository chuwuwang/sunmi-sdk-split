package com.sm.sdk.demo.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class DesAesUtil {
    public static final int DATA_MODE_ECB = 0;
    public static final int DATA_MODE_CBC = 1;
    public static final int DATA_MODE_OFB = 2;
    public static final int DATA_MODE_CFB = 3;

    private DesAesUtil() {
        throw new AssertionError();
    }

    /**
     * AES encrypt data
     *
     * @param key       the  key value
     * @param iv        initialize vector
     * @param dataIn    data to be encrypted
     * @param blockMode block mode, value is ECB,CBC,OFB,CFB
     * @return if success, the encrypted data, otherwise null
     */
    public static byte[] aesEncrypt(byte[] key, byte[] iv, byte[] dataIn, int blockMode) {
        return aesEncryptDecrypt(Cipher.ENCRYPT_MODE, key, iv, dataIn, blockMode);
    }

    /**
     * AES decrypt data
     *
     * @param key       the  key value
     * @param iv        initialize vector
     * @param dataIn    data to be encrypted
     * @param blockMode block mode, value is ECB,CBC,OFB,CFB
     * @return if success, the decrypted data, otherwise null
     */
    public static byte[] aesDecrypt(byte[] key, byte[] iv, byte[] dataIn, int blockMode) {
        return aesEncryptDecrypt(Cipher.DECRYPT_MODE, key, iv, dataIn, blockMode);
    }

    /**
     * DES encrypt data
     *
     * @param key       the  key value
     * @param iv        initialize vector
     * @param dataIn    data to be encrypted
     * @param blockMode block mode, value is ECB,CBC,OFB,CFB
     * @return if success, the encrypted data, otherwise null
     */
    public static byte[] desEncrypt(byte[] key, byte[] iv, byte[] dataIn, int blockMode) {
        return desEncryptDecrypt(Cipher.ENCRYPT_MODE, key, iv, dataIn, blockMode);
    }

    /**
     * DES decrypt data
     *
     * @param key       the  key value
     * @param iv        initialize vector
     * @param dataIn    data to be encrypted
     * @param blockMode block mode, value is ECB,CBC,OFB,CFB
     * @return if success, the decrypted data, otherwise null
     */
    public static byte[] desDecrypt(byte[] key, byte[] iv, byte[] dataIn, int blockMode) {
        return desEncryptDecrypt(Cipher.DECRYPT_MODE, key, iv, dataIn, blockMode);
    }

    /**
     * AES encrypt/decrypt data
     *
     * @param opmode    the operation mode of this cipher
     * @param key       the  key value
     * @param iv        initialize vector
     * @param dataIn    data to be encrypted
     * @param blockMode block mode, value is ECB,CBC,OFB,CFB
     * @return if success, the encrypted/decrypted data, otherwise null
     */
    private static byte[] aesEncryptDecrypt(int opmode, byte[] key, byte[] iv, byte[] dataIn, int blockMode) {
        try {
            if (!checkAesKey(key) || !checkIv(blockMode, iv) || !checkAesDataIn(dataIn) || !checkBlockMode(blockMode)) {
                return null;
            }
            String transformation = "AES/" + getBlockMode(blockMode) + "/NoPadding";
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance(transformation);
            if (blockMode == DATA_MODE_ECB || iv == null) {
                cipher.init(opmode, secretKeySpec);
            } else {
                IvParameterSpec spec = new IvParameterSpec(iv);
                cipher.init(opmode, secretKeySpec, spec);
            }
            return cipher.doFinal(dataIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * DES encrypt/decrypt data
     *
     * @param opmode    the operation mode of this cipher
     * @param key       the  key value
     * @param iv        initialize vector
     * @param dataIn    data to be encrypted
     * @param blockMode block mode, value is ECB,CBC,OFB,CFB
     * @return if success, the encrypted/decrypted data, otherwise null
     */
    private static byte[] desEncryptDecrypt(int opmode, byte[] key, byte[] iv, byte[] dataIn, int blockMode) {
        try {
            if (!checkDesKey(key) || !checkIv(blockMode, iv) || !checkDesDataIn(dataIn) || !checkBlockMode(blockMode)) {
                return null;
            }
            String transformation = "DESede/" + getBlockMode(blockMode) + "/NoPadding";
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "DESede");
            Cipher cipher = Cipher.getInstance(transformation);
            if (blockMode == DATA_MODE_ECB || iv == null) {
                cipher.init(opmode, secretKeySpec);
            } else {
                IvParameterSpec spec = new IvParameterSpec(iv);
                cipher.init(opmode, secretKeySpec, spec);
            }
            return cipher.doFinal(dataIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean checkAesKey(byte[] key) {
        return key != null && (key.length == 16 || key.length == 24 || key.length == 32);
    }

    private static boolean checkDesKey(byte[] key) {
        return key != null && (key.length == 8 || key.length == 16 || key.length == 24);
    }

    private static boolean checkBlockMode(int blockMode) {
        return blockMode >= DATA_MODE_ECB && blockMode <= DATA_MODE_CFB;
    }

    private static boolean checkAesDataIn(byte[] dataIn) {
        return dataIn != null && dataIn.length % 16 == 0;
    }

    private static boolean checkDesDataIn(byte[] dataIn) {
        return dataIn != null && dataIn.length % 8 == 0;
    }

    private static boolean checkIv(int blockMode, byte[] iv) {
        if (blockMode == DATA_MODE_ECB) {
            return true;
        }
        return iv != null && (iv.length == 8 || iv.length == 16);
    }

    private static String getBlockMode(int blockMode) {
        switch (blockMode) {
            case DATA_MODE_ECB:
                return "ECB";
            case DATA_MODE_CBC:
                return "CBC";
            case DATA_MODE_OFB:
                return "OFB";
            case DATA_MODE_CFB:
                return "CFB";
        }
        return "ECB";
    }
}
