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

    public NodeInfo(String id) {
        this.id = id;
    }
}