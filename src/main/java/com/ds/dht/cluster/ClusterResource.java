package com.ds.dht.cluster;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cluster")
public class ClusterResource {

    private static Logger logger = LoggerFactory.getLogger(ClusterResource.class);

    @Autowired
    private ClusterHandler clusterHandler;

    @GetMapping("/info")
    public ResponseEntity<Set<NodeInfo>> getClusterInfo() {
        return ResponseEntity.ok().body(clusterHandler.getClusterInfo());
    }

    @PutMapping("/node")
    public void addNode(@RequestBody NodeInfo nodeInfo) {
        clusterHandler.addNode(nodeInfo);
        logger.info("Added node to cluster : " + nodeInfo);
    }

}
