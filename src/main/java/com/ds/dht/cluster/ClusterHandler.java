package com.ds.dht.cluster;

import com.ds.dht.htable.HTable;
import com.ds.dht.util.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class ClusterHandler {

    private static final Set<NodeInfo> TREE;

    @Autowired
    private RestTemplate restTemplate;

    static {
        TREE = new ConcurrentSkipListSet<>(Comparator.comparing(NodeInfo::getId));
    }

    public void init(Optional<NodeSocket> gatewaySocket) {
        gatewaySocket.ifPresent(gs -> {
            try {
                ResponseEntity<Set<NodeInfo>> entity = restTemplate.exchange(gs.getUrl() + "/cluster/info",
                        HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                        });
                if (entity.getStatusCode() == HttpStatus.OK) {
                    TREE.addAll(entity.getBody());
                    populateKeysFromSuccessor(MyInfo.get());
                    updateMyInfoToNeighbours(gs);
                } else {
                    System.err.println("Bad response from gateway node Help me! I'm independent now : " + entity.getStatusCodeValue());
                    //System.exit(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Cannot connect gateway node. Help me! I'm independent now " + e.getMessage());
                //System.exit(0);
            }
        });
        TREE.add(MyInfo.get());
    }

    private void updateMyInfoToNeighbours(NodeSocket gs) {
        RequestEntity<NodeInfo> requestEntity = RequestEntity
                .put(gs.getUrl() + "/cluster/node")
                .accept(MediaType.APPLICATION_JSON)
                .body(MyInfo.get());
        ResponseEntity<?> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>() {
        });
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            System.out.println("Cannot update my status to neighbours : " + responseEntity.getStatusCodeValue());
        }
    }

    private NodeInfo getSuccessor(NodeInfo nodeInfo) {
        NodeInfo successor = ((ConcurrentSkipListSet<NodeInfo>) TREE).ceiling(nodeInfo);
        if (successor == null) {
            successor = ((ConcurrentSkipListSet<NodeInfo>) TREE).first();
        }
        return successor;
    }

    private NodeInfo getSuccessor(String hash) {
        NodeInfo successor = ((ConcurrentSkipListSet<NodeInfo>) TREE).ceiling(new NodeInfo(hash));
        if (successor == null) {
            successor = ((ConcurrentSkipListSet<NodeInfo>) TREE).first();
        }
        return successor;
    }

    private void populateKeysFromSuccessor(NodeInfo myInfo) {
        ResponseEntity<Map<String, String>> entity = restTemplate.exchange(getSuccessor(myInfo).getSocket().getUrl() + "/share/" + myInfo.getId(),
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });
        if (entity.getStatusCode() == HttpStatus.OK) {
            HTable.putAll(entity.getBody());
        } else {
            System.err.println("Error getting own keys : " + entity.getStatusCode());
            System.exit(0);
        }
    }

    Set<NodeInfo> getClusterInfo() {
        return TREE;
    }

    void addNode(NodeInfo nodeInfo) {
        TREE.add(nodeInfo);
    }

    public NodeInfo getNodeForKey(String key) {
        return getSuccessor(Hash.toSHA1(key));
    }

}
