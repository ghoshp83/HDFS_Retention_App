package com.pralay.core.pralfile;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PralFile {

    private Path path;

    private FileOperation operation;

    // private PralContext pralContext = PralContext.create();
    private FileSystem fs;

    private static final Logger LOG = LoggerFactory.getLogger(PralFile.class);

    public PralFile(FileSystem fs, String pathString, FileOperation op) {

        this.fs = fs;
        path = new Path(pathString);
        operation = op;
    }

    public FSDataInputStream openForRead() throws IOException {
        // pralContext.getStatus().incr_file_r(path.toString(), 1);
        return fs.open(path);
    }

    public FSDataOutputStream openForWrite() throws IOException {

        if (operation == FileOperation.READ) {
            LOG.warn("The file was created with read right only! "
                    + path.toString());
            throw new IOException("The file was created with read right only!");
        }

        FSDataOutputStream result = null;

        if (!fs.exists(path.getParent())) {
            fs.mkdirs(path.getParent());
        }
        if (fs.exists(path)) {
            if (operation == FileOperation.APPEND) {
                result = fs.append(path);
            } else if (operation == FileOperation.OVERWRITE) {
                result = fs.create(path);
            } else {
                throw new IOException("The requested file already exists. "
                        + path.toString());
            }
        } else {
            result = fs.create(path);

        }
        if (result != null) {
            // pralContext.getStatus().incr_file_w(path.toString(), 1);
        }
        return result;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public FileSystem getFs() {
        return fs;
    }

    public void setFs(FileSystem fs) {
        this.fs = fs;
    }

    public FileOperation getOperation() {
        return operation;
    }

}
