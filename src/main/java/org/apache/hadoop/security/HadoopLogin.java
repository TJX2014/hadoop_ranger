package org.apache.hadoop.security;

import org.apache.hadoop.conf.Configuration;
import sun.security.krb5.KrbException;

import java.io.IOException;

public class HadoopLogin {

  public static void main(String[] args) throws IOException, KrbException {
    String krbFile = "/tmp/hdfs_k8s-node1_HADOOP_COM_krb5.conf";
//    System.setProperty("java.security.krb5.conf", krbFile);
    System.setProperty("java.security.krb5.kdc", "k8s-node1");
    System.setProperty("java.security.krb5.realm", "HADOOP.COM");
    sun.security.krb5.Config config = sun.security.krb5.Config.getInstance();

    String defaultRealm = config.getDefaultRealm();
    String kdcList = config.getKDCList(defaultRealm);

    Configuration conf = new Configuration();
    conf.set("hadoop.security.authentication", UserGroupInformation.AuthenticationMethod.KERBEROS.toString());
    conf.set("ipc.client.fallback-to-simple-auth-allowed", "true");
    UserGroupInformation.setConfiguration(conf);

    String authRule = "\n" +
        "            RULE:[2:$1@$0](hdfs@.*HADOOP.COM)s/.*/hdfs/\n" +
        "            RULE:[2:$1@$0](yarn@.*HADOOP.COM)s/.*/yarn/\n" +
        "            DEFAULT\n" +
        "        ";
    HadoopKerberosName.setRules(authRule);

    String loginPrincipal = "hdfs/k8s-node1@HADOOP.COM";
    String keytab = "/tmp/hdfs_k8s-node1_HADOOP_COM.keytab";
    UserGroupInformation.loginUserFromKeytab(loginPrincipal, keytab);
    UserGroupInformation ugi = UserGroupInformation.getLoginUser();

    UserGroupInformation.reset();
    System.out.println(ugi);
  }
}
