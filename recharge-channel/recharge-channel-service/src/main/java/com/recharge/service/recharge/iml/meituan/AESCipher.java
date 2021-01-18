package com.recharge.service.recharge.iml.meituan;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class AESCipher {

    public byte[] encrypt(byte[] key, byte[] input) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        return encrypt(key, input, 0, input.length);
    }

    public byte[] encrypt(byte[] key, byte[] input, int offset, int len) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        checkKeyLen(key);

        SecretKey secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return cipher.doFinal(input, offset, len);
    }

    public byte[] decrypt(byte[] key, byte[] input) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        return decrypt(key, input, 0, input.length);
    }

    public byte[] decrypt(byte[] key, byte[] input, int offset, int len) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        checkKeyLen(key);

        SecretKey secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return cipher.doFinal(input, offset, len);
    }

    private void checkKeyLen(byte[] key) {
        if(key.length < 16 || (key.length % 16) != 0) {
//throw new RuntimeException("Byte length of key must be (bytelen >= 16) and (bytelen mod 16 = 0)");
        }
    }

    public byte[] encrypt(byte[] key, byte[] iv, byte[] input) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        return encrypt(key, iv, input, 0, input.length);
    }

    public byte[] encrypt(byte[] key, byte[] iv, byte[] input, int offset, int len) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        checkKeyLen(key);

        KeyGenerator keyGenerator=KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(key);
        keyGenerator.init(128, random);//key长可设为128，192，256位，这里只能设为128
        IvParameterSpec ivParamSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyGenerator.generateKey(), ivParamSpec);

        return cipher.doFinal(input, offset, len);
    }

    public byte[] decrypt(byte[] key, byte[] iv, byte[] input) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        return decrypt(key, iv, input, 0, input.length);
    }

    public byte[] decrypt(byte[] key, byte[] iv, byte[] input, int offset, int len) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        checkKeyLen(key);

        KeyGenerator keyGenerator=KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(key);
        keyGenerator.init(128, random);//key长可设为128，192，256位，这里只能设为128

        IvParameterSpec ivParamSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keyGenerator.generateKey(), ivParamSpec);

        return cipher.doFinal(input, offset, len);
    }
}