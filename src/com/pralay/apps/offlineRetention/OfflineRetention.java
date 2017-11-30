package com.pralay.apps.offlineRetention;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.hadoop.fs.FileSystem;
import org.apache.zookeeper.KeeperException;
import org.slf4j.LoggerFactory;

import com.pralay.core.pralfile.PralFileFactory;

import org.slf4j.Logger;

public class OfflineRetention {

    InputStream inputStream;
    static Properties prop;

    private static Logger log = LoggerFactory.getLogger(OfflineRetention.class.toString());

    public Properties getPropValues() throws IOException {

    log.debug("loading config.properties file ..");
    String propFileName = "config.properties";

    prop = new Properties();

    inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

    if (inputStream != null) {
    prop.load(inputStream);
    } else {
      log.error("property file '" + propFileName + "' not found in the classpath");
       throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
     }

     return prop;

     }

    public static void main(String[] args)
       throws FileNotFoundException, IOException, InterruptedException, KeeperException {

       FileSystem hdfsFileSystem = null;

       OfflineRetention offRet = new OfflineRetention();

       try {
       hdfsFileSystem = PralFileFactory.getHdfsFileSystem();
        log.debug("HDFS filesystem got intitialized...");
      } catch (IOException e) {

         log.error("Unable to connect to HDFS: " + e.getMessage());
      }
      prop = offRet.getPropValues();
      log.debug("invoking retention period monitoring..");
      MonitorRetentionPeriod mrPeriod = new MonitorRetentionPeriod();
     mrPeriod.monitorRetention(hdfsFileSystem, prop);

    }
}