package com.pralay.apps.offlineRetention;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class HdfsCrudOperations {

    private static Logger log = LoggerFactory.getLogger(HdfsCrudOperations.class.toString());

    void deleteExpiredFiles(FileSystem hdfsFileSystem, List<String> ExpiredFilesList) throws IOException {
        for (String filePath : ExpiredFilesList) {
            String file_path = filePath.substring(0, filePath.lastIndexOf(':'));

           try {
                hdfsFileSystem.delete(new Path(file_path), true);
                log.debug("The file :" + file_path + " is deleted from HDFS. Since retention period is completed..");
            } catch (IOException io) {

                log.error("unable to delete the file ::" + file_path + " in HDFS");
                log.error(io.getMessage());
            }
        }
    }

    void deleteZookeeperPath(ZooKeeper zkConnection, List<String> expiredFilesList)
            throws KeeperException, InterruptedException {

        String file_path = null;

            for (String filePath : expiredFilesList) {
                    try{
                    if (!filePath.contains("NA")) {

                    file_path = filePath.substring(filePath.lastIndexOf(":") + 1);

                    deleteZookeeperPathRecursively(zkConnection, file_path);
                }
             } catch (Exception e) {

            log.error("Exception occured while deleting the folder :" + file_path + " from Zookeeper");
            log.error(e.getMessage());

           }
        }
    }

    void deleteZookeeperPathRecursively(ZooKeeper zkConnection, String path)
            throws KeeperException, InterruptedException {

        final List<String> children = zkConnection.getChildren(path, false);
        if (children.size() > 0) {
            for (final String s : children) {
                deleteZookeeperPathRecursively(zkConnection, createPath(path, s));
            }
        } else {
            final Stat stat = new Stat();
            zkConnection.getData(path, false, stat);
            zkConnection.delete(path, stat.getVersion());
            log.debug("the folder :" + path + " got deleted from znode folder structure.");
        }
    }

    static String createPath(String... pathParts) {
        StringBuilder sb = new StringBuilder();
        for (String s : pathParts) {
            for (String p : s.split("/")) {
                sb.append(p);
                sb.append("/");
            }
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}