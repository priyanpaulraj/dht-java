package com.ds.dht.cluster;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import com.ds.dht.DhtApplication;
import com.ds.dht.client.RestClient;
import com.ds.dht.htable.HTable;
import com.ds.dht.util.Hash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ClusterHandler {

    private static Logger logger = LoggerFactory.getLogger(DhtApplication.class);

    private static final Set<NodeInfo> TREE;

    @Autowired
    private RestClient restClient;

    static {
        TREE = new ConcurrentSkipListSet<>(Comparator.comparing(NodeInfo::getId));
    }

    public void init(Optional<NodeSocket> gatewaySocket) {
        gatewaySocket.ifPresent(gs -> {
            try {
                ResponseEntity<Set<NodeInfo>> entity = restClient.get(gs.getUrl() + "/cluster/info");
                if (entity.getStatusCode() == HttpStatus.OK) {
                    TREE.addAll(entity.getBody());
                    populateKeysFromSuccessor(MyInfo.get());
                    updateMyInfoToNeighbours(entity.getBody());
                } else {
                    logger.error("Bad response from gateway node Help! I'm alone : " + entity.getStatusCodeValue());
                }
            } catch (Exception e) {
                logger.error("Cannot connect gateway node. Help! I'm alone ", e);
            }
        });
        TREE.add(MyInfo.get());
    }

    private void updateMyInfoToNeighbours(Set<NodeInfo> neighbours) {
        neighbours.forEach(neighbour -> {
            ResponseEntity<?> responseEntity = restClient.put(neighbour.getSocket().getUrl() + "/cluster/node",
                    MyInfo.get());
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.error("Cannot update my status to neighbours : " + responseEntity.getStatusCodeValue());
            }
        });
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
        String url = new StringBuilder(getSuccessor(myInfo).getSocket().getUrl())
                .append("/share/")
                .append(URLEncoder.encode(myInfo.getId(), StandardCharsets.UTF_8))
                .toString();
        ResponseEntity<Map<String, String>> entity = restClient.get(url);
        if (entity.getStatusCode() == HttpStatus.OK) {
            HTable.putAll(entity.getBody());
            logger.info("Populating keys from neighbour : " + myInfo + " : " + HTable.keys());
        } else {
            logger.error("Error getting own keys : " + entity.getStatusCode());
            System.exit(0);
        }
    }

    Set<NodeInfo> getClusterInfo() {
        return TREE.stream().map(ni -> {
            try {
                ResponseEntity<Integer> entity = restClient.get(ni.getSocket().getUrl() + "/table/size");
                ni.setNoOfKeys(entity.getStatusCode() == HttpStatus.OK ? entity.getBody() : -1);
                ni.setNodeStatus(NodeStatus.UP);
            } catch (Exception e) {
                ni.setNodeStatus(NodeStatus.DOWN);
                logger.error("Error fetching table size", e);
            }
            return ni;
        }).collect(Collectors.toSet());
    }

    void addNode(NodeInfo nodeInfo) {
        TREE.add(nodeInfo);
    }

    public NodeInfo getNodeForKey(String key) {
        return getSuccessor(String.valueOf(Hash.doHash(key)));
    }

    public Set<NodeInfo> getNSuccessors(NodeInfo fromNode, int n) {
        Set<NodeInfo> successors = new HashSet<>();
        NodeInfo successor = fromNode;
        while (n > 0) {
            successor = ((ConcurrentSkipListSet<NodeInfo>) TREE).higher(successor);
            if (successor == null) {
                successor = ((ConcurrentSkipListSet<NodeInfo>) TREE).first();
            }
            if (successor.equals(fromNode) || successors.contains(successor)) {
                throw new RuntimeException(
                        "Not enough nodes are alive to replicate. Consider changing the replication configuration");
            }
            successors.add(successor);
            n--;
        }
        return successors;
    }

}
