package com.ds.dht.htable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTable {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger eventLogger = LoggerFactory.getLogger("eventLogger");
    private static final Logger logger = LoggerFactory.getLogger(HTable.class);

    private static final Map<String, String> TABLE;

    static {
        TABLE = Collections.synchronizedMap(new HashMap<>());
    }

    public static void put(String k, String v) {
        logEvent(EventOperation.PUT, k, Optional.of(v));
        TABLE.put(k, v);
    }

    public static boolean exists(String k) {
        return TABLE.containsKey(k);
    }

    public static String get(String k) {
        return TABLE.get(k);
    }

    public static String remove(String k) {
        logEvent(EventOperation.DEL, k, Optional.empty());
        return TABLE.remove(k);
    }

    public static void putAll(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public static Set<String> keys() {
        return TABLE.keySet();
    }

    public static Map<String, String> keysToShare(final String nodeId) {
        Set<String> toShare = keys().stream().filter(k -> k.compareTo(nodeId) <= 0).collect(Collectors.toSet());
        Map<String, String> map = new HashMap<>();
        for (String k : toShare) {
            map.put(k, get(k));
            // remove(k); //TODO: atleast n replication
        }
        logger.debug("Sharing keys with new neighbour :" + nodeId + " : " + toShare);
        return map;
    }

    public static int size() {
        return TABLE.size();
    }

    public static void logEvent(EventOperation eventOperation, String key, Optional<String> optionalValue) {
        ObjectNode node = mapper.createObjectNode();
        node.put("dtm", System.currentTimeMillis());
        node.put("opr", eventOperation.toString());
        node.put("key", key);
        if (optionalValue.isPresent()) {
            node.put("val", optionalValue.get());
        }
        eventLogger.info(node.toString());
    }

}
