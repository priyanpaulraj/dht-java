package com.ds.dht.htable;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import com.ds.dht.client.RestClient;
import com.ds.dht.cluster.ClusterHandler;
import com.ds.dht.cluster.MyInfo;
import com.ds.dht.cluster.NodeInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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

@RestController
@RequestMapping("/")
public class HTableResource {

    private static Logger logger = LoggerFactory.getLogger(HTableResource.class);

    @Autowired
    private ClusterHandler clusterHandler;

    @Autowired
    private RestClient restClient;

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
                        restClient.put(successor.getSocket().getUrl() + "/" + key + "?direct=true", value);
                    }
                } else {
                    restClient.put(node.getSocket().getUrl() + "/" + key, value);
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
                return !HTable.exists(key) ? ResponseEntity.notFound().build() : ResponseEntity.ok(HTable.get(key));
            }
            NodeInfo node = clusterHandler.getNodeForKey(key);
            if (node.getId().equals(MyInfo.get().getId())) {
                return !HTable.exists(key) ? ResponseEntity.notFound().build() : ResponseEntity.ok(HTable.get(key));
            } else {
                return getFromNeighbours(node, key);
            }
        } catch (Exception e) {
            logger.error("Unknown error", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/share/{newNodeId}")
    public Map<String, String> share(@PathVariable String newNodeId) {
        newNodeId = URLDecoder.decode(newNodeId, StandardCharsets.UTF_8);
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
            return restClient.get(node.getSocket().getUrl() + "/" + key + "?direct=" + direct,
                    new ParameterizedTypeReference<String>() {
                    });
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        } catch (ResourceAccessException rae) {
            return null;
        }
    }

    private ResponseEntity<String> getFromNeighbours(NodeInfo node, String key) {
        ResponseEntity<String> responseEntity = get(node, key, false);
        if (responseEntity == null) {
            Set<NodeInfo> successors = clusterHandler.getNSuccessors(node, replicationNodeCount - 1);
            System.out.println(successors);
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

}
