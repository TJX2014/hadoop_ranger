package org.apache.hadoop.security;

import java.io.IOException;

public class HadoopLogin {

  public static void main(String[] args) throws IOException {
    String authRule = "\n" +
        "            RULE:[2:$1@$0](hdfs@.*HADOOP.COM)s/.*/hdfs/\n" +
        "            RULE:[2:$1@$0](yarn@.*HADOOP.COM)s/.*/yarn/\n" +
        "            DEFAULT\n" +
        "        ";
    HadoopKerberosName.setRules(authRule);

    String krbFile = "/tmp/hdfs_k8s-node1_HADOOP_COM_krb5.conf";
    System.setProperty("java.security.krb5.conf", krbFile);

    String loginPrincipal = "hdfs/k8s-node1@HADOOP.COM";
    String keytab = "/tmp/hdfs_k8s-node1_HADOOP_COM.keytab";
    UserGroupInformation.loginUserFromKeytab(loginPrincipal, keytab);
    UserGroupInformation ugi = UserGroupInformation.getLoginUser();

    System.out.println(ugi);
  }
}
