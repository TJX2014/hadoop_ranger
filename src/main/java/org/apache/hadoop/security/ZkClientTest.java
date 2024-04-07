package org.apache.hadoop.security;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.hadoop.hive.shims.Utils;
import org.apache.zookeeper.CreateMode;

public class ZkClientTest {

  public static void main(String[] args) throws Exception {
    System.setProperty("java.security.krb5.realm", "HADOOP.COM");
    System.setProperty("java.security.krb5.kdc", "k8s-node1");

    Utils.setZookeeperClientKerberosJaasConfig("hive-metastore/k8s-node1@HADOOP.COM", "/usr/kerberos_hms/hive-metastore.keytab");

    String zooKeeperEnsemble = "k8s-node1:2181";
    int sessionTimeout = 1200000;
    int baseSleepTime = 1000;
    int maxRetries = 3;
    CuratorFramework zkClient = CuratorFrameworkFactory.builder().connectString(zooKeeperEnsemble).sessionTimeoutMs(sessionTimeout).retryPolicy(new ExponentialBackoffRetry(baseSleepTime, maxRetries)).build();
    zkClient.start();
    String rootNamespace = "hiveserver2";

    ((ACLBackgroundPathAndBytesable)zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)).forPath("/" + rootNamespace);
  }
}
