package com.ds.dht.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/cluster")
public class ClusterResource {

    @Autowired
    private ClusterHandler clusterHandler;

    @GetMapping("/info")
    public ResponseEntity<Set<NodeInfo>> getClusterInfo() {
        return ResponseEntity.ok().body(clusterHandler.getClusterInfo());
    }

    @PutMapping("/node")
    public void addNode(@RequestBody NodeInfo nodeInfo){
        clusterHandler.addNode(nodeInfo);
    }

}
