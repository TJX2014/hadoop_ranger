package org.apache.hadoop.hdfs.server.datanode;

import org.apache.hadoop.conf.Configuration;

import java.io.IOException;

public class DataNodeLocal {

    public static void main(String[] args) throws IOException {
        Configuration conf = new Configuration();
        conf.set("dfs.datanode.hostname", "0.0.0.0");
        DataNode dn = DataNode.instantiateDataNode(args, conf);
        dn.runDatanodeDaemon();
        dn.join();
    }
}
