package com.ds.dht.htable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.recovery.ResilientFileOutputStream;

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

    public static void populateFromEventLogs() throws IOException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();) {
                Appender<ILoggingEvent> appender = index.next();
                if (appender instanceof FileAppender && appender.getName().equals("eventLog")) {
                    FileAppender<ILoggingEvent> fa = (FileAppender<ILoggingEvent>) appender;
                    ResilientFileOutputStream rfos = (ResilientFileOutputStream) fa.getOutputStream();
                    Files.lines(Paths.get(rfos.getFile().getPath())).forEach(HTable::loadTable);
                }
            }
        }
    }

    private static void loadTable(String event) {
        try {
            ObjectNode node = mapper.readValue(event, ObjectNode.class);
            String opr = node.get("opr").textValue();
            String key = node.get("key").textValue();
            if (opr.equals(EventOperation.PUT.toString())) {
                String val = node.get("val").textValue();
                TABLE.put(key, val);
            } else if (opr.equals(EventOperation.DEL.toString())) {
                TABLE.remove(key);
            }
        } catch (JsonProcessingException e) {
            logger.error("Error parsing event log", e);
        }
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
