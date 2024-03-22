import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.namenode.NameNode;

public class NameNodeLocal {
    public static void main(String[] args) throws Exception {
//        new MiniDFSCluster().startDataNodes(new Configuration());
//        System.setProperty("java.security.krb5.realm", "HADOOP.COM");
//        System.setProperty("java.security.krb5.kdc", "localhost");
        Configuration conf = new Configuration();
//        String[] argv = new String[]{"-format"};
//        NameNode.main(argv);
//        new NameNode(conf)
//        DefaultMetricsSystem.setMiniClusterMode(true);
        new NameNode(conf).createNameNode(args, conf).join();
//        DataNode.main(args);
//        FileSystem fs = FileSystem.get(conf);
//        FileStatus[] statuses = fs.listStatus(new Path("/"));
//        System.out.println(statuses);
    }
}
