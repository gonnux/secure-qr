package com.gonnux.secureqr.biz;

import java.util.Base64;

public class CipherText {
    private byte[] salt;
    private byte[] iv;
    private byte[] encryptedData;

    public CipherText(byte[] saltInput, byte[] ivInput, byte[] encryptedDataInput) {
        salt = saltInput;
        iv = ivInput;
        encryptedData = encryptedDataInput;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getIv() {
        return iv;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    public static CipherText decode(String encoded) {
        Base64.Decoder decoder = Base64.getDecoder();
        String[] pieces = encoded.split("\\.");

        if (pieces.length != 4 || !pieces[0].equals("SecureQR"))
            throw new IllegalArgumentException("Invalid CipherText Input");
        return new CipherText(decoder.decode(pieces[1]), decoder.decode(pieces[2]), decoder.decode(pieces[3]));
    }

    public String encode() {
        Base64.Encoder encoder = Base64.getEncoder();
        return String.join(".", "SecureQR", encoder.encodeToString(salt), encoder.encodeToString(iv), encoder.encodeToString(encryptedData));
    }
}
