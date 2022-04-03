package com.ds.dht.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hash {

    private static Logger logger = LoggerFactory.getLogger(Hash.class);

    public static String doHash(String value) {
        try {
            byte[] bytesOfMessage = value.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] theMD5digest = md.digest(bytesOfMessage);
            return new String(Base64.getEncoder().encode(theMD5digest));
        } catch (NoSuchAlgorithmException e) {
            logger.error("", e);
            return null;
        }
    }

}
