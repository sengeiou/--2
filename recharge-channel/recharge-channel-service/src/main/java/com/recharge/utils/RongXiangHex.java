package com.recharge.utils;

import java.util.Random;

/**
 * reference apache commons <a 
 * href="http://commons.apache.org/codec/">http://commons.apache.org/codec/</a> 
 *
 * @author Aub
 *
 */
public class RongXiangHex {

    /**
     * ���ڽ���ʮ�������ַ��������Сд�ַ�����
     */
    public static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * ���ڽ���ʮ�������ַ�������Ĵ�д�ַ�����
     */
    public static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * ���ֽ�����ת��Ϊʮ�������ַ�����
     *
     * @param data
     *            byte[]
     * @return ʮ������char[]
     */
    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    /**
     * ���ֽ�����ת��Ϊʮ�������ַ�����
     *
     * @param data
     *            byte[]
     * @param toLowerCase
     *            <code>true</code> ������Сд��ʽ �� <code>false</code> �����ɴ�д��ʽ
     * @return ʮ������char[]
     */
    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * ���ֽ�����ת��Ϊʮ�������ַ�����
     *
     * @param data
     *            byte[]
     * @param toDigits
     *            ���ڿ��������char[]
     * @return ʮ������char[]
     */
    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    /**
     * ���ֽ�����ת��Ϊʮ�������ַ���
     *
     * @param data
     *            byte[]
     * @return ʮ������String
     */
    public static String encodeHexStr(byte[] data) {
        return encodeHexStr(data, true);
    }

    /**
     * ���ֽ�����ת��Ϊʮ�������ַ���
     *
     * @param data
     *            byte[]
     * @param toLowerCase
     *            <code>true</code> ������Сд��ʽ �� <code>false</code> �����ɴ�д��ʽ
     * @return ʮ������String
     */
    public static String encodeHexStr(byte[] data, boolean toLowerCase) {
        return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * ���ֽ�����ת��Ϊʮ�������ַ���
     *
     * @param data
     *            byte[]
     * @param toDigits
     *            ���ڿ��������char[]
     * @return ʮ������String
     */
    protected static String encodeHexStr(byte[] data, char[] toDigits) {
        return new String(encodeHex(data, toDigits));
    }

    /**
     * ��ʮ�������ַ�����ת��Ϊ�ֽ�����
     *
     * @param data
     *            ʮ������char[]
     * @return byte[]
     * @throws RuntimeException
     *             ���Դʮ�������ַ�������һ����ֵĳ��ȣ����׳�����ʱ�쳣
     */
    public static byte[] decodeHex(char[] data) {

        int len = data.length;

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * ��ʮ�������ַ�ת����һ������
     *
     * @param ch
     *            ʮ������char
     * @param index
     *            ʮ�������ַ����ַ������е�λ��
     * @return һ������
     * @throws RuntimeException
     *             ��ch����һ���Ϸ���ʮ�������ַ�ʱ���׳�����ʱ�쳣
     */
    protected static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch
                    + " at index " + index);
        }
        return digit;
    }
    
    /**
     * ǩ����Կ�Ľ����㷨
     * 
     * @param src  16���Ƶ��ַ���
     * @return
     * @return String  16���Ƶ��ַ���
     * @description 
     * @version 1.0
     */
    public static String HexDecode(String src)
    {
        if (src.length()%2 != 0){
            return null;
        }
        int len=src.length();
        byte[] hexsrcs=hexstr2byte(src);        
        byte[] hexdsts=new byte[len/2];
        for(int i=0;i<len/2;i++)
        {
            hexdsts[i]=(byte)(hexsrcs[i]-i*3+i-23);
        }

        return encodeHexStr(hexdsts).toUpperCase();
    }
    
    /**
     * ת��Ϊ16���Ƶļ����㷨
     * 
     * @param src  16���Ƶ��ַ���
     * @return
     * @return String  16���Ƶ��ַ���
     * @description 
     * @version 1.0

     */
    public static String HexEncode(String src)
    {
        if (src.length()%2 != 0){
            return null;
        }
        int len=src.length();
        byte[] hexsrcs=hexstr2byte(src);        
        byte[] hexdsts=new byte[len/2];
        for(int i=0;i<len/2;i++)
        {
            hexdsts[i]=(byte)(hexsrcs[i]+i*3-i+23);
        }

        return encodeHexStr(hexdsts).toUpperCase();
    }
    
    /*
     * 16�����ַ���תbyte[]
     * �磺��F56573��ת����Ϊ ��0xF5, 0x65, 0x73��
     */
    public static byte[] hexstr2byte(String s){
        if (s.length()%2 != 0){
            return null;
        }
        int len = s.length()/2;
        byte[] arr = new byte[len];
        
        for(int i = 0; i < len; i++)
        {
            arr[i] = (byte) Integer.parseInt(s.substring(2*i, 2*i+2), 16);
        }

        return arr;
    }
    
    public static String getRadom32str(){
        StringBuffer sbStr=new StringBuffer();
        int max=15;
        int min=0;
        Random random = new Random();        
        for(int i=0;i<32;i++){
            int s = random.nextInt(max)%(max-min+1) + min;
            sbStr.append(DIGITS_UPPER[s]);
        }        
        return sbStr.toString();        
    }

    public static void main(String[] args) {
        String srcStr = "62DEDDFD9B8F84311271A0F82F62BD59";
        String encodeStr = encodeHexStr(srcStr.getBytes());
        String decodeStr = new String(decodeHex(encodeStr.toCharArray()));
        System.out.println("ת��ǰ��" + srcStr);
        System.out.println("ת����" + encodeStr);
        System.out.println("��ԭ��" + decodeStr);
        
        System.out.println("���ܣ�" + HexEncode("62DEDDFD9B8F84311271A0F82F62BD59"));
        System.out.println("���ܣ�" + HexDecode("5E4341766494529C"));
    }

}