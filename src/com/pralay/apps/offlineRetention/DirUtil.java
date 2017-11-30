package com.pralay.apps.offlineRetention;

import org.apache.hadoop.fs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirUtil {

    private static Logger log = LoggerFactory.getLogger(DirUtil.class.toString());

    //    mock subject
//    given /root, returns subdir1, subdir2
    List<String> getDirs(String dir, FileSystem hdfsFs, final String filter) {
        PathFilter pf = new PathFilter() {
            public boolean accept(Path file) {
                Pattern p = Pattern.compile(filter);
                Matcher m = p.matcher(file.getName());
                return m.matches();
            }
        };

        try {
            FileStatus[] dirs = hdfsFs.listStatus(new Path(dir), pf);
            Path[] paths = FileUtil.stat2Paths(dirs);

            List<String> dirList = new ArrayList<>();
            for (Path p : paths) {
                dirList.add(p.getName());
            }

//            successful return
            return dirList;
        } catch (IOException e1) {
            log.error("failed to list dir status from hdfs dir " + dir, e1);
        }

//        error return empty list
        return new ArrayList<>();
    }
}
