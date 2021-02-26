package com.recharge.utils;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.SecureRandom;

public class RongXiangDESUtil {

    // ���� �����㷨
    private final static String DES = "DES/ECB/NoPadding";

    /**
     * ��Կ�㷨
     */
    private static final String KEY_ALGORITHM = "DES";

    /**
     * �����ܳ׽���DES����
     *
     * @param key  �ܳ�
     * @param info Ҫ���ܵ���Ϣ
     * @return String ���ܺ����Ϣ
     */
    public static String encryptToDES( String info,String key) {
        key = new String(RongXiangHex.decodeHex(key.toCharArray()));
        // ��������������� (RNG),(���Բ�д)
        SecureRandom sr = new SecureRandom();
        // ����Ҫ���ɵ�����
        byte[] cipherByte = null;
        try {
            // ������Կ
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), KEY_ALGORITHM);

            // �õ�����/������
            Cipher c1 = Cipher.getInstance(DES);
            // ��ָ������Կ��ģʽ��ʼ��Cipher����
            // ����:(ENCRYPT_MODE, DECRYPT_MODE, WRAP_MODE,UNWRAP_MODE)
            c1.init(Cipher.ENCRYPT_MODE, secretKey, sr);
            // ��Ҫ���ܵ����ݽ��б��봦��,

            // ��λ
            byte[] tmp = info.getBytes();
            int x = tmp.length % 8;
            byte[] newByte = new byte[tmp.length + 8 -x];

            System.arraycopy(tmp, 0, newByte, 0, tmp.length);
            cipherByte = c1.doFinal(newByte);
        } catch (Exception e) {
//           throw new ZCLinkerException("����ʧ��",e);
            e.printStackTrace();
        }
        // �������ĵ�ʮ��������ʽ

        return RongXiangHex.encodeHexStr(cipherByte, RongXiangHex.DIGITS_UPPER);
    }

    public static void main(String[] args) throws Exception {
        // String data = "123 456";
        String key = "4735764763345C3B";

        String data = "{\"Cost\":\"����50.0\",\"RetMsg\":\"����ʧ��[0207-2-0001]\",\"Operators\":\"2\",\"OrderNo\":\"100560401102013101215423087305\",\"OrderTime\":\"20131012154230\",\"PhoneNo\":\"111111111111\",\"RechargeType\":\"01\",\"RetURL\":\"http://202.102.53.149:8020/liantongguangfang.jsp<@#>S1309251930365\"}";

        String test = encryptToDES(data,key);
        System.out.println(test);
        System.err.println(decrypt(test, key));

        System.out.println(decrypt("96c1520af289535cbd3f781980045be463e87bb0198b4861812d004e01cbc911a75fae4332cfc4187feac5de7368f5e1508ba53df5dfe0905ae961535eda2dbeee659d77be54cbda8c9f53ceb8d429ab69b34c54eaac9777bedcf1b8bffd0e3bb4708cbd3d06d6adbdcfd0f1878ac40697f92063809a960f986289d43badf16d846f510b0b6fe6b6349e45492dd5c461cccb44a3e4bfd3b4e7ee91bac50191efc47f2ad21bbbb449971cac8a78b23fd5c159e33dd951568d2d177bdf75a33c9b2850e0d8b738f1818b09f5006f21161282ba2d015d3e8bb9819863003b16a8a6", key));

    }

    /**
     * Description ���ݼ�ֵ���н���
     *
     * @param data
     * @param key  ���ܼ�byte����
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static String decrypt(String data, String key)  {

        try {
            key = new String(RongXiangHex.decodeHex(key.toCharArray()));
            byte[] desc = RongXiangHex.decodeHex(data.toCharArray());
            // ִ�в���
            return new String(decrypt(desc, key.getBytes()));
        } catch (Exception e) {
//            throw new ZCLinkerException("����ʧ��",e);
            return "";
        }
    }

    /**
     * Description ���ݼ�ֵ���н���
     *
     * @param data
     * @param key  ���ܼ�byte����
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // ����һ�������ε������Դ
        SecureRandom sr = new SecureRandom();

        // ������Կ
        SecretKey securekey = new SecretKeySpec(key, KEY_ALGORITHM);

        // Cipher����ʵ����ɽ��ܲ���
        Cipher cipher = Cipher.getInstance(DES);

        // ����Կ��ʼ��Cipher����
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }
}