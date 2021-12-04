package com.gonnux.secureqr.biz;

import android.util.Base64;

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
        String[] pieces = encoded.split("\\.");

        if (pieces.length != 4 || !pieces[0].equals("SecureQR"))
            throw new IllegalArgumentException("Invalid CipherText Input");
        return new CipherText(
            Base64.decode(pieces[1], Base64.DEFAULT),
            Base64.decode(pieces[2], Base64.DEFAULT),
            Base64.decode(pieces[3], Base64.DEFAULT)
        );
    }

    public String encode() {
        return String.join(
            ".",
            "SecureQR",
            Base64.encodeToString(salt, Base64.DEFAULT),
            Base64.encodeToString(iv, Base64.DEFAULT),
            Base64.encodeToString(encryptedData, Base64.DEFAULT)
        );
    }
}
