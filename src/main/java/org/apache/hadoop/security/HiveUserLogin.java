package org.apache.hadoop.security;

import com.sun.security.auth.module.Krb5LoginModule;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.authentication.util.KerberosName;
import org.apache.thrift.TException;
import sun.security.krb5.KrbException;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HiveUserLogin {

  public static void main(String[] args) throws IOException, KrbException, TException, LoginException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
//    System.setProperty("java.security.krb5.realm", "HADOOP.COM");
//    System.setProperty("java.security.krb5.kdc", "k8s-node1");
    String krb5File = "/tmp/hive-metastore_k8s-node1_HADOOP_COM_1711971291276.krb5conf";
    String defaultKrb5 = "/usr/kerberos_hms/krb5.conf";
    System.setProperty("java.security.krb5.conf", krb5File);
//    loginOutside();
    String principal = "hive-metastore/k8s-node1@HADOOP.COM";
    String keyTab = "/tmp/hive-metastore_k8s-node1_HADOOP_COM_1711967470821.keytab";

    UserGroupInformation.loginUserFromKeytab(principal, keyTab);

    KerberosName.setRules("DEFAULT");

    Krb5LoginModule loginModule = new Krb5LoginModule();
    Subject subject = new Subject();
    Map<String, ?> sharedState = new HashMap<>();
    Map<String, String> options = new HashMap<>();

    options.put("principal", principal);
    options.put("storeKey", "true");
    options.put("doNotPrompt", "true");
    options.put("keyTab", "/usr/kerberos_hms/hive-metastore.keytab");
    options.put("useKeyTab", "true");
    options.put("refreshKrb5Config", "true");
    loginModule.initialize(subject, null, sharedState, options);
    loginModule.login();
    loginModule.commit();

    Constructor<UserGroupInformation> ugiConstructor =
        UserGroupInformation.class.getDeclaredConstructor(Subject.class);
    ugiConstructor.setAccessible(true);
    User user = new User(principal, UserGroupInformation.AuthenticationMethod.KERBEROS, null);
    subject.getPrincipals().add(user);
    UserGroupInformation ugi = new UserGroupInformation(subject);

    ugi.reloginFromKeytab();
//    UserGroupInformation ugi = ugiConstructor.newInstance(subject);
//    new org.apache.hadoop.security.User("");
    System.out.println(ugi);
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
    List<String> catalogs = metaStoreClient.getCatalogs();
    System.out.println(catalogs);
  }
}
