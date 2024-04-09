package org.apache.hadoop.security;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.thrift.TException;
import sun.security.krb5.KrbException;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class HiveConn {

  public static void main(String[] args) throws IOException, KrbException, TException, LoginException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, SQLException {

//    HiveConf.getVar(new HiveConf(), HiveConf.ConfVars.HIVE_AUTHORIZATION_MANAGER);

    System.setProperty("java.security.krb5.realm", "HADOOP.COM");
    System.setProperty("java.security.krb5.kdc", "k8s-node1");
    String principal = "hive-metastore/k8s-node1@HADOOP.COM";
    String keyTab = "/usr/kerberos_hms/hive-metastore.keytab";
    UserGroupInformation.loginUserFromKeytab(principal, keyTab);

    Connection connection = DriverManager.getConnection(
        "jdbc:hive2://10.201.0.213:10000/test1;principal=hive-server2/_HOST@HADOOP.COM");

//    Connection connection2 = DriverManager.getConnection(
//        "jdbc:hive2://k8s-node1:10000/test1;principal=hive-server2/_HOST@HADOOP.COM");

    System.out.println(connection);
  }

  private static void loginOutside() throws IOException, KrbException, TException {
    sun.security.krb5.Config config = sun.security.krb5.Config.getInstance();
    UserGroupInformation.loginUserFromKeytab(
        "hive-metastore/k8s-node1@HADOOP.COM", "/usr/kerberos_hms/hive-metastore.keytab");
//    UserGroupInformation
    System.out.println("login:" + UserGroupInformation.getLoginUser());
//    UserGroupInformation.getLoginUser().logoutUserFromKeytab();
//    UserGroupInformation.setLoginUser(null);
    UserGroupInformation.setLoginUser(null);
    System.out.println("logout:" + UserGroupInformation.getLoginUser());

    HiveConf hiveSiteConf = new HiveConf();
    hiveSiteConf.set("hive.metastore.uris", "thrift://10.201.0.213:9093");
    hiveSiteConf.set("hive.metastore.sasl.enabled", "true");
    HiveMetaStoreClient metaStoreClient = new HiveMetaStoreClient(hiveSiteConf);
//    List<String> catalogs = metaStoreClient.getCatalogs();
//    System.out.println(catalogs);
  }
}
