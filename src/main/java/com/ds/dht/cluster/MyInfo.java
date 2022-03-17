package com.ds.dht.cluster;

import com.ds.dht.util.Hash;

import java.util.UUID;

public class MyInfo {

    private static NodeInfo myInfo;

    public static NodeSocket mySocket;

    public static NodeInfo get() {
        if (myInfo == null) {
            String hash = String.valueOf(Hash.doHash(UUID.randomUUID().toString()));
            myInfo = new NodeInfo(hash, mySocket);
        }
        return myInfo;
    }

}
