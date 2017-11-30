package com.pralay.apps.offlineRetention;

import java.io.IOException;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class ZookeeperConnection implements Watcher {

    final int CONNECTION_TIMEOUT = 2000;
    ZooKeeper zooKeeper;
    private static Logger log = LoggerFactory.getLogger(ZookeeperConnection.class.toString());

    public ZooKeeper getZookeeperConnection(String hostString) {

        try {
        zooKeeper = new ZooKeeper(hostString, CONNECTION_TIMEOUT, this);

        log.debug("Zookeeper connection established..");
        } catch (IOException e) {
        log.error("Not able to establish connection to Zookeeper..");
        }
        return zooKeeper;
   }
@Override
   public void process(WatchedEvent arg0) {
   }
}