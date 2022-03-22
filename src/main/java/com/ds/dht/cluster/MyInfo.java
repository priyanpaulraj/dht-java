package com.ds.dht.cluster;

import java.util.UUID;

import com.ds.dht.util.Hash;

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

    public static void setMyInfo(NodeInfo nodeInfo) {
        myInfo = nodeInfo;
    }

}
