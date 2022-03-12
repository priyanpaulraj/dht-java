package com.ds.dht.htable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HTable {

    private static final Map<String, String> MAP;

    static {
        MAP = Collections.synchronizedMap(new HashMap<>());
    }

    public static void put(String k, String v) {
        MAP.put(k, v);
    }

    public static boolean exists(String k) {
        return MAP.containsKey(k);
    }

    public static String get(String k) {
        return MAP.get(k);
    }

    public static String remove(String k) {
        return MAP.remove(k);
    }

    public static void putAll(Map<String, String> map) {
        MAP.putAll(map);
    }

    public static Set<String> keys() {
        return MAP.keySet();
    }

    public static Map<String, String> keysToShare(final String nodeId) {
        Set<String> toShare = MAP.keySet().stream().filter(k -> k.compareTo(nodeId) <= 0).collect(Collectors.toSet());
        Map<String, String> map = new HashMap<>();
        for (String k : toShare) {
            map.put(k, MAP.get(k));
            MAP.remove(k);
        }
        return map;
    }

}
