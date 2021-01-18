package com.recharge.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author qi.cao
 */
public class ChiPeiUtils {

    /**
     * 话费加密方法
     *
     * @param encData
     * @param secretKey
     * @param vector
     * @return
     */
    public static String encrypt(String encData, String secretKey, String vector) {
        try {
            if (secretKey == null) {
                return null;
            }
            if (secretKey.length() != 16) {
                return null;
            }
            byte[] raw = secretKey.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// "算法/模式/补码方式"
            IvParameterSpec iv = new IvParameterSpec(vector.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(encData.getBytes());
            return encode(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Base64转换用字符
     */
    private static char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();

    static private byte[] codes = new byte[256];
    /**
     * 将原始数据编码为base64编码
     *
     * @param data
     * @return
     */
    public static String encode(byte[] data) {
        char[] out = new char[((data.length + 2) / 3) * 4];
        for (int i = 0, index = 0; i < data.length; i += 3, index += 4) {
            boolean quad = false;
            boolean trip = false;
            int val = (0xFF & (int) data[i]);
            val <<= 8;
            if ((i + 1) < data.length) {
                val |= (0xFF & (int) data[i + 1]);
                trip = true;
            }
            val <<= 8;
            if ((i + 2) < data.length) {
                val |= (0xFF & (int) data[i + 2]);
                quad = true;
            }
            out[index + 3] = alphabet[(quad ? (val & 0x3F) : 64)];
            val >>= 6;
            out[index + 2] = alphabet[(trip ? (val & 0x3F) : 64)];
            val >>= 6;
            out[index + 1] = alphabet[val & 0x3F];
            val >>= 6;
            out[index + 0] = alphabet[val & 0x3F];
        }
        return String.valueOf(out);
    }

    /**
     * 将base64编码的数据解码成原始数据
     */
    static public byte[] decode(String str) {
        char[] data = str.toCharArray();
        int len = ((data.length + 3) / 4) * 3;
        if (data.length > 0 && data[data.length - 1] == '=') {
            --len;
        }
        if (data.length > 1 && data[data.length - 2] == '=') {
            --len;
        }
        byte[] out = new byte[len];
        int shift = 0;
        int accum = 0;
        int index = 0;
        for (int ix = 0; ix < data.length; ix++) {
            int value = codes[data[ix] & 0xFF];
            if (value >= 0) {
                accum <<= 6;
                shift += 6;
                accum |= value;
                if (shift >= 8) {
                    shift -= 8;
                    out[index++] = (byte) ((accum >> shift) & 0xff);
                }
            }
        }
        if (index != out.length) {
            throw new Error("miscalculated data length!");
        }
        return out;
    }

    public static String decrypt(String encrypted, String secretKey, String vector) {
        try {
            byte[] keyb = secretKey.getBytes();
            SecretKeySpec skey = new SecretKeySpec(keyb, "AES");
            Cipher dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(vector.getBytes());
            dcipher.init(Cipher.DECRYPT_MODE, skey, iv);

            byte[] clearbyte = dcipher.doFinal(decode(encrypted));
            return new String(clearbyte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String strToMd5(String str, String charSet) {
        String md5Str = null;
        if (str != null && str.length() != 0) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(str.getBytes(charSet));
                byte b[] = md.digest();
                int i;
                StringBuffer buf = new StringBuffer("");
                for (int offset = 0; offset < b.length; offset++) {
                    i = b[offset];
                    if (i < 0) {
                        i += 256;
                    }
                    if (i < 16) {
                        buf.append("0");
                    }
                    buf.append(Integer.toHexString(i));
                }
                md5Str = buf.toString();
            } catch (NoSuchAlgorithmException e) {
                System.out.println("MD5加密发生异常。加密串：" + str);
            } catch (UnsupportedEncodingException e2) {
                System.out.println("MD5加密发生异常。加密串：" + str);
            }
        }
        return md5Str;
    }
}
