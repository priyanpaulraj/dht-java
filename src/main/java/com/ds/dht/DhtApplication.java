package com.ds.dht;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.ds.dht.cluster.ClusterHandler;
import com.ds.dht.cluster.MyInfo;
import com.ds.dht.cluster.NodeSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DhtApplication implements ApplicationRunner {

    private static Logger logger = LoggerFactory.getLogger(DhtApplication.class);

    @Autowired
    private ClusterHandler clusterHandler;

    public static void main(String[] args) {
        SpringApplication.run(DhtApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, String> argMap = new HashMap<>(System.getenv());
        for (String ag : args.getSourceArgs()) {
            ag = ag.substring(2);
            String[] agArr = ag.split("=");
            argMap.put(agArr[0], agArr[1]);
        }
        if (!argMap.containsKey("server.address") || !argMap.containsKey("server.port")) {
            logger.error("Provide server ip and port as program args");
            System.exit(0);
        }
        MyInfo.mySocket = new NodeSocket(argMap.get("server.address"), Integer.parseInt(argMap.get("server.port")));
        Optional<NodeSocket> gatewayNode = Optional.empty();
        if (argMap.containsKey("gateway.address") && argMap.containsKey("gateway.port")) {
            gatewayNode = Optional
                    .of(new NodeSocket(argMap.get("gateway.address"), Integer.parseInt(argMap.get("gateway.port"))));
        }
        clusterHandler.init(gatewayNode);
    }
}
