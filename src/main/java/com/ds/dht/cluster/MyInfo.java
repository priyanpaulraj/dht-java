package com.ds.dht.cluster;

import com.ds.dht.util.Hash;

public class MyInfo {

    private static NodeInfo myInfo;

    public static NodeSocket mySocket;

    public static NodeInfo get() {
        if (myInfo == null) {
            String hash = Hash.toSHA1(mySocket.getUrl());
            myInfo = new NodeInfo(hash, mySocket);
        }
        return myInfo;
    }

}
