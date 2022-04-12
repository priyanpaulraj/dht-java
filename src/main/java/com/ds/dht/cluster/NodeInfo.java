package com.ds.dht.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeInfo {
    private String id;
    private NodeSocket socket;
    private int noOfKeys;
    private NodeStatus nodeStatus;

    public NodeInfo(String id, NodeSocket socket) {
        this.id = id;
        this.socket = socket;
    }

    public NodeInfo(String id) {
        this.id = id;
    }

}