package org.apache.hadoop.security;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FsShell;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.security.PrivilegedAction;

public class DFSClientTest {

  public static void main(String[] args) throws Exception {
    UserGroupInformation.getLoginUser();

//    System.setProperty("java.security.krb5.conf", "/tmp/hdfs_k8s-node1_HADOOP_COM_krb5.conf");
    Configuration conf = new Configuration();
//    conf.set("hadoop.security.authentication", UserGroupInformation.AuthenticationMethod.KERBEROS.toString());
    conf.set("fs.defaultFS", "hdfs://10.201.0.227:8020");
//    conf.set("hadoop.security.authentication", "kerberos");
    conf.set("ipc.client.fallback-to-simple-auth-allowed", "true");
//    UserGroupInformation.loginUserFromKeytab("hdfs/k8s-node1@HADOOP.COM", "/tmp/hdfs_k8s-node1_HADOOP_COM.keytab");
    FileSystem fs = FileSystem.get(conf);

    String[] chownArgs = new String[]{"-chown", "-R", "root",
        "hdfs://10.201.0.227:8020/bucket/dlink-workbench-3d38903c"};

//    String[] chmodArgs = new String[] {"-chmod", "-R", "707", "/bucket/CHECKPOINT"};
    org.apache.hadoop.util.ToolRunner.run(conf, new FsShell(), chownArgs);
//    org.apache.hadoop.fs.FsShell.main(chownArgs);

    FileStatus[] fileStatuses = fs.listStatus(new Path("hdfs://10.201.0.227:8020/bucket/"));

    UserGroupInformation ugi = UserGroupInformation.getLoginUser();
    UserGroupInformation.setLoginUser(null);
    UserGroupInformation.reset();

    Configuration simpleConf = new Configuration();
    simpleConf.set("hadoop.security.authentication", "simple");
    UserGroupInformation.setConfiguration(simpleConf);
    ugi.doAs((PrivilegedAction<Object>) () -> {
      try {
        fs.create(new Path(
            "hdfs://k8s-node1:8020/usr/dlink-workbench-9f66a665/xiaoxing_iceberg3_682/icn_test1.db/aaa-dd3c89a65f1449f9b8899c0dda7b2"), true);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return null;
    });
  }
}
