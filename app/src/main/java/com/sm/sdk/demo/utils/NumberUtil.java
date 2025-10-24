package com.sm.sdk.demo.utils;

import android.text.TextUtils;

public final class NumberUtil {

    private NumberUtil() {
        throw new AssertionError("create instance of NumberUtil is prohibited");
    }

    /** 解析十进制byte类型的数据 */
    public static byte parseByte(String src) {
        return _parseByte(src, 10, (byte) 0);
    }

    /** 解析十六进制byte类型的数据 */
    public static byte parseHexByte(String src) {
        return _parseByte(src, 16, (byte) 0);
    }

    /** 解析十进制char类型的数据 */
    public static char parseChar(String src) {
        return _parseChar(src, (char) 0);
    }

    /** 解析十进制short类型的数据 */
    public static short parseShort(String src) {
        return _parseShort(src, 10, (short) 0);
    }

    /** 解析十六进制short类型的数据 */
    public static short parseHexShort(String src) {
        return _parseShort(src, 16, (short) 0);
    }

    /** 解析十进制int类型的数据 */
    public static int parseInt(String src) {
        return _parseInteger(src, 10, 0);
    }

    /** 解析十六进制int类型的数据 */
    public static int parseHexInt(String src) {
        return _parseInteger(src, 16, 0);
    }

    /** 解析十进制long类型的数据 */
    public static long parseLong(String src) {
        return _parseLong(src, 10, 0);
    }

    /** 解析十六进制long类型的数据 */
    public static long parseHexLong(String src) {
        return _parseLong(src, 16, 0);
    }

    /** 解析float类型的数据 */
    public static float parseFloat(String src) {
        return _parseFloat(src, 0.0f);
    }

    /** 解析double类型的数据 */
    public static double parseDouble(String src) {
        return _parseDouble(src, 0.0);
    }

    /** 解析boolean类型的数据 */
    public static boolean parseBoolean(String src) {
        return _parseBoolean(src, false);
    }

    /** 解析十进制byte类型的数据 */
    public static byte parseByte(String src, byte defValue) {
        return _parseByte(src, 10, defValue);
    }

    /** 解析十六进制byte类型的数据 */
    public static byte parseHexByte(String src, byte defValue) {
        return _parseByte(src, 16, defValue);
    }

    /** 解析十进制char类型的数据 */
    public static char parseChar(String src, char defValue) {
        return _parseChar(src, defValue);
    }

    /** 解析十进制short类型的数据 */
    public static short parseShort(String src, short defValue) {
        return _parseShort(src, 10, defValue);
    }

    /** 解析十六进制short类型的数据 */
    public static short parseHexShort(String src, short defValue) {
        return _parseShort(src, 16, defValue);
    }

    /** 解析十进制int类型的数据 */
    public static int parseInt(String src, int defValue) {
        return _parseInteger(src, 10, defValue);
    }

    /** 解析十六进制int类型的数据 */
    public static int parseHexInt(String src, int defValue) {
        return _parseInteger(src, 16, defValue);
    }

    /** 解析十进制long类型的数据 */
    public static long parseLong(String src, long defValue) {
        return _parseLong(src, 10, defValue);
    }

    /** 解析十六进制long类型的数据 */
    public static long parseHexLong(String src, long defValue) {
        return _parseLong(src, 16, defValue);
    }

    /** 解析float类型的数据 */
    public static float parseFloat(String src, float defValue) {
        return _parseFloat(src, defValue);
    }

    /** 解析double类型的数据 */
    public static double parseDouble(String src, double defValue) {
        return _parseDouble(src, defValue);
    }

    /** 解析boolean类型的数据 */
    public static boolean parseBoolean(String src, boolean defValue) {
        return _parseBoolean(src, defValue);
    }

    /** float转int */
    public static int floatToInt(float src) {
        return (int) (src + 0.5f);
    }

    /** double转int */
    public static int doubleToInt(double src) {
        return (int) (src + 0.5D);
    }

    /** 解析byte数据 */
    private static byte _parseByte(String src, int radix, byte defValue) {
        try {
            if (!TextUtils.isEmpty(src)) {
                return Byte.parseByte(src, radix);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    /** 解析char数据 */
    private static char _parseChar(String src, char defValue) {
        try {
            if (!TextUtils.isEmpty(src)) {
                return src.charAt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    /** 解析short数据 */
    private static short _parseShort(String src, int radix, short defValue) {
        try {
            if (!TextUtils.isEmpty(src)) {
                return Short.parseShort(src, radix);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    /** 解析int类型的数据 */
    private static int _parseInteger(String src, int radix, int defValue) {
        try {
            if (!TextUtils.isEmpty(src)) {
                return Integer.parseInt(src, radix);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    /** 解析long类型的数据 */
    private static long _parseLong(String src, int radix, long defValue) {
        try {
            if (!TextUtils.isEmpty(src)) {
                return Long.parseLong(src, radix);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    /** 解析float类型的数据 */
    public static float _parseFloat(String src, float defValue) {
        try {
            if (!TextUtils.isEmpty(src)) {
                return Float.parseFloat(src);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    /** 解析double类型的数据 */
    public static double _parseDouble(String src, double defValue) {
        try {
            if (!TextUtils.isEmpty(src)) {
                return Double.parseDouble(src);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    /** 解析boolean类型的数据 */
    public static boolean _parseBoolean(String src, boolean defValue) {
        try {
            if (!TextUtils.isEmpty(src)) {
                return Boolean.parseBoolean(src);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }
}
