package org.apache.hadoop.security;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Catalog;
import org.apache.thrift.TException;

import java.io.IOException;
import java.security.PrivilegedAction;
import java.util.List;

public class HMSClient {

  public static void main(String[] args) throws TException, IOException {
    System.setProperty("java.security.krb5.realm", "HADOOP.COM");
    System.setProperty("java.security.krb5.kdc", "k8s-node1");
    String principal = "hive-metastore/k8s-node1@HADOOP.COM";
    String keytab = "/usr/kerberos_hms/hive-metastore.keytab";
    UserGroupInformation.loginUserFromKeytab(principal, keytab);
    UserGroupInformation ugi = UserGroupInformation.getLoginUser();

    UserGroupInformation.reset();
    Configuration conf = new Configuration();
    conf.set("hive.metastore.uris", "thrift://10.201.0.250:30470");
    conf.set("hadoop.security.authentication", "simple");
    UserGroupInformation.setConfiguration(conf);
    HiveMetaStoreClient client = new HiveMetaStoreClient(conf);
    List<String> catalogs = client.getCatalogs();

    List<String> catalogs1 = ugi.doAs((PrivilegedAction<List<String>>) () -> {
      try {
        return client.getCatalogs();
      } catch (TException e) {
        throw new RuntimeException(e);
      }
    });
    List<String> databases = client.getAllDatabases("hdfs_df_wd_148");
    System.out.println(databases);
  }
}
