package com.ds.dht.htable;

import com.ds.dht.cluster.ClusterHandler;
import com.ds.dht.cluster.MyInfo;
import com.ds.dht.cluster.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/")
public class HTableResource {

    @Autowired
    private ClusterHandler clusterHandler;

    @Autowired
    private RestTemplate restTemplate;

    @PutMapping(value = "/{key}", consumes = MediaType.TEXT_PLAIN_VALUE)
    public void put(@PathVariable String key, @RequestBody String value) {
        NodeInfo node = clusterHandler.getNodeForKey(key);
        if (node.getId().equals(MyInfo.get().getId())) {
            HTable.put(key, value);
        } else {
            restTemplate.put(node.getSocket().getUrl() + "/" + key, value);
        }
    }

    @GetMapping(value = "/{key}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> get(@PathVariable String key) {
        NodeInfo node = clusterHandler.getNodeForKey(key);
        if (node.getId().equals(MyInfo.get().getId())) {
            if (!HTable.exists(key))
                return ResponseEntity.notFound().build();
            return ResponseEntity.ok(HTable.get(key));
        } else {
            try {
                return restTemplate.getForEntity(node.getSocket().getUrl() + "/" + key, String.class);
            } catch (HttpStatusCodeException e) {
                return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
                        .body(e.getResponseBodyAsString());
            }
        }
    }

    @GetMapping("/share/{newNodeId}")
    public Map<String, String> share(@PathVariable String newNodeId) {
        return HTable.keysToShare(newNodeId);
    }

    @GetMapping("/keys")
    public Set<String> listKeys() {
        return HTable.keys();
    }

}
