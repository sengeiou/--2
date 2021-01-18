package com.recharge.utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.*;

/**
 * @author Administrator
 * @create 2020/12/31 11:27
 */
public class LiFangSignUtils {
    public static String sha1(String str) throws IOException {
        return byte2hex(getSHA1Digest(str));
    }

    private static byte[] getSHA1Digest(String data) throws IOException {
        byte[] bytes;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            bytes = md.digest(data.getBytes("utf-8"));
        } catch (GeneralSecurityException gse) {
            throw new IOException(gse);
        }
        return bytes;
    }

    private static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }
        return sign.toString();
    }

    public static String sign(Map<String, String> param, String secret) throws IOException {
        if (param == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        List<String> paramNames = new ArrayList<>(param.size());
        paramNames.addAll(param.keySet());
        Collections.sort(paramNames);
        sb.append(secret);
        for (String paramName : paramNames) {
            sb.append(paramName).append(param.get(paramName));
        }
        sb.append(secret);
        return sha1(sb.toString());
    }

    public static void main(String[] args) throws IOException {
        Map<String, String> param = new HashMap<>();
        param.put("access_token", "7466bdfc5f79a7fe1defd9a5880a4b84");
        param.put("v", "1.1");
        param.put("method", "bm.elife.recharge.mobile.getItemInfo");
        param.put("timestamp", "1428488009985");
        param.put("mobileNo", "13888888888");
        param.put("rechargeAmount", "100");
        System.out.println(sign(param, "test"));
    }
}
