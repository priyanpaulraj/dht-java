package com.ds.dht.htable;

import java.util.Map;
import java.util.Set;

import com.ds.dht.cluster.ClusterHandler;
import com.ds.dht.cluster.MyInfo;
import com.ds.dht.cluster.NodeInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/")
public class HTableResource {

    private static Logger logger = LoggerFactory.getLogger(HTableResource.class);

    @Autowired
    private ClusterHandler clusterHandler;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.replication.nodes.count}")
    private int replicationNodeCount;

    @PutMapping(value = "/{key}", consumes = MediaType.TEXT_PLAIN_VALUE)
    public void put(@PathVariable String key, @RequestBody String value,
            @RequestParam(name = "direct", defaultValue = "false", required = false) boolean direct) {
        try {
            if (direct) {
                HTable.put(key, value);
            } else {
                NodeInfo node = clusterHandler.getNodeForKey(key);
                if (node.getId().equals(MyInfo.get().getId())) {
                    HTable.put(key, value);
                    Set<NodeInfo> successors = clusterHandler.getNSuccessors(node, replicationNodeCount - 1);
                    for (NodeInfo successor : successors) {
                        restTemplate.put(successor.getSocket().getUrl() + "/" + key + "?direct=true", value);
                    }
                } else {
                    restTemplate.put(node.getSocket().getUrl() + "/" + key, value);
                }
            }
        } catch (Exception e) {
            logger.error("Unknown error", e);
        }
    }

    @GetMapping(value = "/{key}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> get(@PathVariable String key,
            @RequestParam(name = "direct", defaultValue = "false", required = false) boolean direct) {
        try {
            if (direct) {
                if (!HTable.exists(key))
                    return ResponseEntity.notFound().build();
                return ResponseEntity.ok(HTable.get(key));
            }

            NodeInfo node = clusterHandler.getNodeForKey(key);
            if (node.getId().equals(MyInfo.get().getId())) {
                if (!HTable.exists(key))
                    return ResponseEntity.notFound().build();
                return ResponseEntity.ok(HTable.get(key));
            } else {
                ResponseEntity<String> responseEntity = get(node, key, false);
                if (responseEntity == null) {
                    Set<NodeInfo> successors = clusterHandler.getNSuccessors(node, replicationNodeCount - 1);
                    for (NodeInfo successor : successors) {
                        responseEntity = get(successor, key, true);
                        if (responseEntity != null) {
                            return responseEntity;
                        }
                    }
                } else {
                    return responseEntity;
                }
                return ResponseEntity.internalServerError().build();
            }
        } catch (Exception e) {
            logger.error("Unknown error", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/share/{newNodeId}")
    public Map<String, String> share(@PathVariable String newNodeId) {
        return HTable.keysToShare(newNodeId);
    }

    @GetMapping("/table/keys")
    public Set<String> listKeys() {
        return HTable.keys();
    }

    @GetMapping("/table/size")
    public int size() {
        return HTable.size();
    }

    private ResponseEntity<String> get(NodeInfo node, String key, boolean direct) {
        try {
            return restTemplate.getForEntity(node.getSocket().getUrl() + "/" + key + "?direct=" + direct, String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        } catch (ResourceAccessException rae) {
            return null;
        }
    }

}
