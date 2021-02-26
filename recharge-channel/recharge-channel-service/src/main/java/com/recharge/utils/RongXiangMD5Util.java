/*
 * �������� 2013-10-17
 */
package com.recharge.utils;


import java.security.MessageDigest;
public class RongXiangMD5Util {
    public final static String MD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       
        try {
            byte[] btInput = s.getBytes();
            // ���MD5ժҪ�㷨�� MessageDigest ����
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // ʹ��ָ�����ֽڸ���ժҪ
            mdInst.update(btInput);
            // �������
            byte[] md = mdInst.digest();
            // ������ת����ʮ�����Ƶ��ַ�����ʽ
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void main(String[] args) {
        System.out.println(RongXiangMD5Util.MD5("ChannelID1006UserCust1001BusiType0202Data210FBE744B6E8E3430FCE55D1CE296976D81BBD3C4D93B1B5D109FDBF1FAF091483B264E3F140993DC10C7966B892F20C05ADF0C9CF3F4A801DD89234E40C1AB96F340E8C4E7B1FDAE4611F1D24492D96F3DE623CA73491FC56E04601B4F4FEDFFBED4CA425FD2DC5905F44F8358E8299DCA00B0272F8EBDF71EBF3659CE0592C0FE6268740573CEB94F5FBE54D1C04A9D7DDD6F5890D09F55F9A1B01A00ABB16DD14078556FF32995A966495699342A72B012039A78BF0ED01BBF6CA5F324B1399457859D79229954492306B63F1DEB3BFAF22765C558616F0E848AC150466EA3ABACEDB5D4C2EE"));
        System.out.println(RongXiangMD5Util.MD5("����"));
    }
}