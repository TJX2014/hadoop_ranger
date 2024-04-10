package org.apache.hadoop.security;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.hadoop.hive.shims.Utils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public class ZkClientTest {

  public static void main(String[] args) throws Exception {
    System.setProperty("java.security.krb5.realm", "HADOOP.COM");
    System.setProperty("java.security.krb5.kdc", "k8s-node1");

//    Utils.setZookeeperClientKerberosJaasConfig("hive-server2/k8s-node1@HADOOP.COM", "/usr/kerberos_hms/hive-server2.keytab");
    Utils.setZookeeperClientKerberosJaasConfig("hive-server2/k8s_local@HADOOP.COM", "/usr/kerberos_hms/hive-server2.keytab");

    String zooKeeperEnsemble = "k8s-node1:2181";
    int sessionTimeout = 1200000;
    int baseSleepTime = 1000;
    int maxRetries = 3;
    CuratorFramework zooKeeperClient = CuratorFrameworkFactory.builder().connectString(zooKeeperEnsemble).sessionTimeoutMs(sessionTimeout).retryPolicy(new ExponentialBackoffRetry(baseSleepTime, maxRetries)).build();
    zooKeeperClient.start();

    String znodeData = "hive.server2.instance.uri=k8s-node3:10000;hive.server2.authentication=NONE;hive.server2.transport.mode=binary;hive.server2.thrift.sasl.qop=auth;hive.server2.thrift.bind.host=k8s-node3;hive.server2.thrift.port=10000;hive.server2.use.SSL=false";
    byte[] znodeDataUTF8 = znodeData.getBytes(Charset.forName("UTF-8"));
    String pathPrefix = "/hiveserver2/serverUri=k8s-local:10000;version=3.1.2;sequence=";
    PersistentEphemeralNode znode = new PersistentEphemeralNode(zooKeeperClient, PersistentEphemeralNode.Mode.EPHEMERAL_SEQUENTIAL, pathPrefix, znodeDataUTF8);
    znode.start();
    System.out.println("result:" + znode.waitForInitialCreate(15, TimeUnit.SECONDS));;
  }



  private static void createDataNode(CuratorFramework zooKeeperClient) throws Exception {
    String zooKeeperNamespace = "hs2";
    String serverNode = "bb";
    String path = "/" + zooKeeperNamespace + "/" + serverNode;
    String content = "server01:1000";

    Stat stat = zooKeeperClient.checkExists().forPath(path);
    if (stat == null) {
      zooKeeperClient.create().withMode(CreateMode.PERSISTENT).forPath(path);
    }

//    zooKeeperClient.getData().usingWatcher(new MyCuratorWatcher()).forPath(path);

    zooKeeperClient.setData().forPath(path, content.getBytes(Charset.forName("UTF-8")));

    String dataStr = new String(zooKeeperClient.getData().forPath(path), Charset.forName("UTF-8"));
    System.out.println(dataStr);
  }

  private static class MyCuratorWatcher implements CuratorWatcher {
    public void process(WatchedEvent event) throws Exception {
      System.out.println("path:" + event.getPath());
      System.out.println("event:" + event);
    }
  }
}
