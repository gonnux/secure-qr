package com.gonnux.secureqr.biz;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    public static final int AES_KEY_SIZE = 256;
    public static final int GCM_IV_SIZE = 12;
    public static final int GCM_TAG_SIZE = 16;
    public static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    public static final String PBKDF_ALGORITHM = "PBKDF2WithHmacSHA256";

    private static SecretKey getKeyFromPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return new SecretKeySpec(
                SecretKeyFactory.getInstance(PBKDF_ALGORITHM).generateSecret(
                        new PBEKeySpec(password.toCharArray(), salt, 65536, AES_KEY_SIZE)
                ).getEncoded(), "AES"
        );
    }

    private static GCMParameterSpec toIv(byte[] iv) {
        return new GCMParameterSpec(GCM_TAG_SIZE * 8, iv);
    }

    private static byte[] generateRandomBytes(int size) {
        byte[] bytes = new byte[size];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    private static byte[] generateIv() {
        return generateRandomBytes(GCM_IV_SIZE);
    }

    private static byte[] generateSalt() {
        return generateRandomBytes(16);
    }

    public static String encrypt(String data, String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        byte[] salt = generateSalt();
        byte[] iv = generateIv();
        cipher.init(Cipher.ENCRYPT_MODE, getKeyFromPassword(password, salt), toIv(iv));
        return new CipherText(salt, iv, cipher.doFinal(data.getBytes())).encode();
    }

    public static String decrypt(String data, String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        CipherText cipherText = CipherText.decode(data);
        cipher.init(Cipher.DECRYPT_MODE, getKeyFromPassword(password, cipherText.getSalt()), toIv(cipherText.getIv()));
        return new String(cipher.doFinal(cipherText.getEncryptedData()));
    }
}
