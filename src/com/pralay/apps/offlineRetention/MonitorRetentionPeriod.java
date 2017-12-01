package com.pralay.apps.offlineRetention;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pralay.core.configuration.PralConfiguration;
import com.pralay.core.configuration.ServerAddr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MonitorRetentionPeriod implements Watcher {

    public Map<String, String> dirMap;
    public List<String> ExpiredFilesList;
    private static Logger log = LoggerFactory.getLogger(MonitorRetentionPeriod.class.toString());
    ZookeeperConnection zkConn = new ZookeeperConnection();
    ZooKeeper zkConnection;
    PralConfiguration pralConfiguration = new PralConfiguration();

    public void monitorRetention(FileSystem hdfsFileSystem, Properties prop)
            throws FileNotFoundException, IOException, InterruptedException, KeeperException {

        List<ServerAddr> host_name = pralConfiguration.getZookeeperHosts();

        for (ServerAddr host : host_name) {
            String host_String = host.getHost() + ":" + host.getPort();
            zkConnection = zkConn.getZookeeperConnection(host_String);
            if (zkConnection.getSessionId() > 0) {
                break;
            }
        }

//        dirMap is a map of <directory name> to <retention days>
//          ex: /user/eea/eea_data/test, 10
        dirMap = getDirAndRetentionTime(prop, hdfsFileSystem);
        log.debug("loaded all directories with retention period..");
        HdfsCrudOperations hdfsco = new HdfsCrudOperations();
        ExpiredFilesList = getExpiredFilesList(hdfsFileSystem, dirMap);
        log.info("Expired Files size => " + ExpiredFilesList.size());
        if (ExpiredFilesList.size() > 0) {

            log.debug("got the expired files list..");
            log.debug("started deleting the expired files in HDFS...");

            hdfsco.deleteExpiredFiles(hdfsFileSystem, ExpiredFilesList);
            hdfsco.deleteZookeeperPath(zkConnection, ExpiredFilesList);
            ExpiredFilesList.clear();
        }

    }

    private List<String> getExpiredFilesList(FileSystem hdfsFileSystem, Map<String, String> dirMap2)
            throws FileNotFoundException, IOException {

        Iterator it = dirMap2.entrySet().iterator();
        long modificationTime = 0;
        ExpiredFilesList = new ArrayList<String>();

        while (it.hasNext()) {

            Map.Entry pair = (Map.Entry) it.next();
            Path folderPath;
            boolean variable_String_flg = false;

            String pre_timestamp_folder_path = null;
            String post_timestamp_folder_path = null;

            if (pair.getKey().toString().contains("${TIMESTAMP}") || pair.getKey().toString().contains("${DATE}")) {

                if (pair.getKey().toString().contains("${TIMESTAMP}")) {
                    pre_timestamp_folder_path = pair.getKey().toString().substring(0,
                            pair.getKey().toString().indexOf("/${TIMESTAMP}"));
                    post_timestamp_folder_path = pair.getKey().toString()
                            .substring(pair.getKey().toString().indexOf("${TIMESTAMP}") + 12);
                } else {

                    pre_timestamp_folder_path = pair.getKey().toString().substring(0,
                            pair.getKey().toString().indexOf("/${DATE}"));
                    post_timestamp_folder_path = pair.getKey().toString()
                            .substring(pair.getKey().toString().indexOf("${DATE}") + 7);
                }

                folderPath = new Path(pre_timestamp_folder_path);

            } else {
                folderPath = new Path(pair.getKey().toString());
            }

            String znode_dir = null;
            long retentionPeriodInDays;

            if (pair.getValue().toString().contains(":")) {
                String[] value = pair.getValue().toString().split(":");
                znode_dir = value[1];
                retentionPeriodInDays = Long.parseLong(value[0]);

            } else {
                retentionPeriodInDays = Long.parseLong(pair.getValue().toString());
            }

            FileStatus[] fileList = hdfsFileSystem.listStatus(folderPath);

            Path[] paths = FileUtil.stat2Paths(fileList);
            for (Path folderName : paths) {

                log.debug("folder name = " + folderName.toString());

                //if (hdfsFileSystem.isDirectory(new Path(folderName.toString()))) {

                    String folder_name = null;
                    String folder_name_1 = null;

                    if (folderName.toString().contains("/")) {
                        folder_name = folderName.toString().substring(folderName.toString().lastIndexOf('/') + 1);
                        folder_name_1 = folder_name;
                    }

                    if (folder_name.contains("=")) {
                        String[] folder = folder_name.split("=");
                        folder_name = folder[1];
                    }
                    if (folder_name.contains("_")) {
                        String[] folder = folder_name.split("_");
                        //folder_name = folder[1];
                        folder_name = folder[folder.length-1];
                        if(folder_name.contains("."))
                           folder_name = folder_name.substring(0,folder_name.lastIndexOf("."));
                    }
                    
                    System.out.println("length: "+folder_name.length());

                    if (folder_name.matches("[0-9]+")) {
                        if (folder_name.length() == 10 || folder_name.length() == 13) {

                            String path = null;
                            boolean isExpired = isExpired(folder_name, new Date(), retentionPeriodInDays, "unix");

                            if (isExpired) {
                                if (!(znode_dir == null)) {
                                    if (variable_String_flg) {
                                        path = folderName.toString() + post_timestamp_folder_path + ":" + znode_dir
                                                + "/" + folder_name_1;
                                    } else {
                                        path = folderName.toString() + ":" + znode_dir + "/" + folder_name_1;
                                    }

                                } else {
                                    if (variable_String_flg) {
                                        path = folderName.toString() + post_timestamp_folder_path + ":NA";
                                    } else {
                                        path = folderName.toString() + ":NA";
                                    }
                                }
                                ExpiredFilesList.add(path);
                            }

                        } else if (folder_name.length() == 8) {

                            boolean isExpired = isExpired(folder_name, new Date(), retentionPeriodInDays, "yyyyMMdd");
                            if (isExpired) {

                                String path = null;
                                if (!(znode_dir == null)) {

                                    if (variable_String_flg) {
                                        path = folderName.toString() + post_timestamp_folder_path + ":" + znode_dir
                                                + "/" + folder_name_1;
                                    } else {
                                        path = folderName.toString() + ":" + znode_dir + "/" + folder_name_1;
                                    }

                                } else {
                                    if (variable_String_flg) {
                                        path = folderName.toString() + post_timestamp_folder_path + ":NA";
                                    } else {
                                        path = folderName.toString() + ":NA";
                                    }
                                }

                                ExpiredFilesList.add(path);
                            }

                        }else if (folder_name.length() == 17) {
                            boolean isExpired = isExpired(folder_name, new Date(), retentionPeriodInDays, "yyyyMMddHHmmssSSS");
                            if (isExpired) {
                                String path = null;
                                if (!(znode_dir == null)) {
                                    if (variable_String_flg) {
                                        path = folderName.toString() + post_timestamp_folder_path + ":" + znode_dir
                                                + "/" + folder_name_1;
                                    } else {
                                        path = folderName.toString() + ":" + znode_dir + "/" + folder_name_1;
                                    }

                                } else {
                                    if (variable_String_flg) {
                                        path = folderName.toString() + post_timestamp_folder_path + ":NA";
                                    } else {
                                        path = folderName.toString() + ":NA";
                                    }
                                }
                                ExpiredFilesList.add(path);
                            }

                        }
                    }
                //}
            }
        }
        return ExpiredFilesList;
    }

    /**
     * @param dirTimeStr
     * @param now
     * @param retentionDays
     * @param pattern       - pattern to interpret the dirTimeStr field.  Can be either "unix" or
     *                      something parseable by SimpleDateFormat.  If "unix", assumes it's unix
     *                      timestamp w/o msec.
     * @return
     */
    boolean isExpired(String dirTimeStr, Date now, long retentionDays, String pattern) {

        long dirTimeMsec = 0;
        boolean parseFailed = false;

        if (pattern.equals("unix")) {
            try {
                if(dirTimeStr.length()==10)
                    dirTimeMsec = Long.parseLong(dirTimeStr) * 1000; // convert to msec
                else
                    dirTimeMsec = Long.parseLong(dirTimeStr); // Multiplication is not required as it is already in msec
            } catch (NumberFormatException e) {
                log.error("failed to parse timestamp as unix timestamp: " + dirTimeStr
                        + ", this dir will not be deleted", e);
                parseFailed = true;
            }
        }

//        pattern is some SDF parseable format
        else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                Date dirTime = sdf.parse(dirTimeStr);
                dirTimeMsec = dirTime.getTime();

            } catch (ParseException | IllegalArgumentException e) {
                log.error("timestamp has the wrong format, must be "+pattern+": " + dirTimeStr
                        + ", this dir will not be deleted", e);
                parseFailed = true;
            }
        }

        if (parseFailed)
            return false; // if we can't parse the timestamp, then don't delete the directory, thus return false

        long nowMsec = now.getTime();
        long retentionInMsec = retentionDays * 24 * 3600 * 1000;
        return (nowMsec - dirTimeMsec) > retentionInMsec;

    }

    private Map<String, String> getDirAndRetentionTime(Properties prop, FileSystem hdfsFs)
            throws IOException {

        dirMap = new HashMap<String, String>();
        List<String> dirList = new ArrayList<String>();
        Enumeration<?> e = prop.propertyNames();
        DirUtil util = new DirUtil();

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.endsWith("_DIR")) {
                dirList.add(key);
            }
        }

//        to handle directories like this
//        /user/eea/stuff/*/foo/<timestamp>
//        TEST_DIR=/user/eea/stuff
//        TEST_DIR_FILTER=.+/foo
//        where .+ is java regexp
        for (String dir : dirList) {

            String dir_retention = dir + "_RETENTION_DAYS";
            String znode_dir = dir + "_ZNODE";
            String filter_dir = dir + "_FILTER";

//            handle filter
            if (prop.containsKey(filter_dir)) {

                String filterStr = prop.getProperty(filter_dir);
                String[] filters = filterStr.split("/");
                List<String> q = new ArrayList<>();
                q.addAll(Arrays.asList(filters));

//                for entry test_DIR = /user/eea/eea_data/test
//                dir is test_DIR (the key), need to convert to /user/eea/...
                String tmpDir = prop.getProperty(dir);

                DirTree root = new DirTree(tmpDir);
                populateDirTree(root, hdfsFs, q, 0, util, tmpDir);
                List<String> expandedDirs = new ArrayList<>();
                getLeafNodes(root, expandedDirs);
                for (String d : expandedDirs) {

                    log.debug("expanded dir = " + d);
                    if (prop.containsKey(znode_dir)) {
                        dirMap.put(d, prop.getProperty(dir_retention) + ":" + prop.getProperty(znode_dir));
                    } else {
                        dirMap.put(d, prop.getProperty(dir_retention));
                    }

                }

            }
//            no filter, existing behavior
            else {
                if (prop.containsKey(znode_dir)) {
                    dirMap.put(prop.getProperty(dir), prop.getProperty(dir_retention) + ":" + prop.getProperty(znode_dir));
                } else {
                    dirMap.put(prop.getProperty(dir), prop.getProperty(dir_retention));
                }
            }
        }

        return dirMap;
    }

    /**
     * @param root - the root node obviously
     * @param dirs - this is the return value, it gets filled with the built dir structure,
     *             so init with an empty List
     */
    void getLeafNodes(DirTree root, List<String> dirs) {

        if (root == null)
            return;

        if (root.getChildren().isEmpty()) {
            dirs.add(root.getNode());
            return;
        }

        for (DirTree node : root.getChildren()) {
            getLeafNodes(node, dirs);
        }
    }

    /**
     * Builds a tree.  Each node's value is the directory structure from the root
     * example: root node = /root
     * its childrent are /root/a and /root/b
     * which has children /root/a/a1, /root/a/a2
     * <p>
     * To get the directory name from this tree, just get all the leaf nodes
     *
     * @param node        - init w/ the root node
     * @param hdfsFs
     * @param filters
     * @param filterIndex - caller init to 0, yet another counter to keep track of things in recursion
     * @param util        - an object so we can mock it
     * @param d           - keep track of the dir name so far, caller inits to the value of the root node
     */
    void populateDirTree(DirTree node, FileSystem hdfsFs, List<String> filters,
                         int filterIndex, DirUtil util, String d) {

//        filters is implemented as queue,
//        filterIndex points to the current element
//        test if filter is "empty"
        if (node == null || filters.size() <= filterIndex) {
            return;
        }

        String filter = filters.get(filterIndex);
        List<String> subDirList = util.getDirs(node.getNode(), hdfsFs, filter);
        for (String subdir : subDirList) {
            String thisNodeValue = d + "/" + subdir;
            DirTree t = new DirTree(thisNodeValue);
            node.addChild(t);
            populateDirTree(t, hdfsFs, filters, filterIndex + 1, util, thisNodeValue);
        }
    }

    @Override
    public void process(WatchedEvent arg0) {
    }

}
