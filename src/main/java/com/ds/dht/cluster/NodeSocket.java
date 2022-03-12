package com.ds.dht.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeSocket {
    private String ip;
    private int port;

    public String getUrl(){
        return "http://"+ip+":"+port;
    }

}
