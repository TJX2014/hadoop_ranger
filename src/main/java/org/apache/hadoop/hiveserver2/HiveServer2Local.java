package org.apache.hadoop.hiveserver2;

import org.apache.hadoop.hive.shims.Utils;
import org.apache.hive.service.server.HiveServer2;

import java.io.IOException;

public class HiveServer2Local {

  public static void main(String[] args) throws IOException {
    System.setProperty("java.security.krb5.realm", "HADOOP.COM");
    System.setProperty("java.security.krb5.kdc", "k8s-node1");

    Utils.setZookeeperClientKerberosJaasConfig("hive-server2/k8s-node1@HADOOP.COM", "/usr/kerberos_hms/hive-server2.keytab");

    HiveServer2.main(args);
  }
}
