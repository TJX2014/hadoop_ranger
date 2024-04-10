package org.apache.hadoop.hiveserver2;

import org.apache.hadoop.EnvUtils;
import org.apache.hadoop.hive.shims.Utils;
import org.apache.hive.service.server.HiveServer2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HiveServer2Local {

  public static void main(String[] args) throws IOException {
    System.setProperty("java.security.krb5.realm", "HADOOP.COM");
    System.setProperty("java.security.krb5.kdc", "k8s-node1");

    Map<String, String> env = new HashMap<>();
    env.put("HIVE_SERVER2_THRIFT_PORT", "10001");
    EnvUtils.setEnv(env, false);

//    Utils.setZookeeperClientKerberosJaasConfig("hive-server2/k8s-node1@HADOOP.COM", "/usr/kerberos_hms/hive-server2.keytab");

    HiveServer2.main(args);
  }
}
