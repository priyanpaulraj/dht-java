package com.ds.dht.util;

public class Hash {

    public static String toSHA1(String value) {
//        try {
//            MessageDigest md = MessageDigest.getInstance("SHA-1");
//            return Base64.getEncoder().encodeToString(md.digest(value.getBytes(StandardCharsets.UTF_8)));
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            return null;
//        }
        return String.valueOf(value.hashCode());
    }

}
