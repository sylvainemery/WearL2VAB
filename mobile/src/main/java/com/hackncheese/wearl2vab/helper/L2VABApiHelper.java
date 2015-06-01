package com.hackncheese.wearl2vab.helper;

import java.util.Random;


public class L2VABApiHelper {

    public static String getCryptedSalt(String password, String salt) {
        return EncodeHelper.base64(EncodeHelper.sha512((new StringBuilder()).append(password).append("{").append(salt).append("}").toString()));
    }

    public static String getSecuredHeader(String email, String password, String salt) {
        String saltCrypted = getCryptedSalt(password, salt);
        int nonce = Math.abs((new Random(System.currentTimeMillis())).nextInt(9999));
        String created = DateHelper.getFormattedNow("yyyy-MM-dd'T'HH:mm:ssZ");
        String digest = EncodeHelper.base64(EncodeHelper.byteToHex(EncodeHelper.sha1((new StringBuilder()).append(nonce).append(created).append(saltCrypted).toString())));
        String securedHeader = (new StringBuilder()).append("UserToken email=\"").append(email).append("\", ").append("nonce=\"").append(nonce).append("\", ").append("created=\"").append(created).append("\", ").append("digest=\"").append(digest).append("\"").toString();

        return securedHeader;
    }
}
