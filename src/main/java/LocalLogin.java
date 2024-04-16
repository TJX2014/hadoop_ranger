import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

public class LocalLogin {

  private static final String KRB5_CONF = "KRB5_CONF";
  private static final String KRB5_PRINCIPAL = "KRB5_PRINCIPAL";
  private static final String KRB5_KEYTAB = "KRB5_KEYTAB";

  public static void main(String[] args) throws IOException {
    String krb5Conf = System.getenv(KRB5_CONF);
    String krb5Principal = System.getenv(KRB5_PRINCIPAL);
    String krb5Keytab = System.getenv(KRB5_KEYTAB);
    if (krb5Conf != null) {
      System.setProperty("java.security.krb5.conf", krb5Conf);
    }
    System.out.println("before login:" + UserGroupInformation.getLoginUser());
    System.out.println("krb5Conf:" + krb5Conf + ";principal:" + krb5Principal + ";keytab:" + krb5Keytab);
    long begin = System.currentTimeMillis();
    Configuration conf = new Configuration();
//    System.setProperty("java.security.krb5.realm", "HADOOP.COM");
    conf.set("hadoop.security.authentication", "kerberos");
    UserGroupInformation.setConfiguration(conf);
    UserGroupInformation.loginUserFromKeytab(krb5Principal, krb5Keytab);
    long end = System.currentTimeMillis();
    System.out.println("cost:" + (end-begin) + "ms");
    System.out.println("after login:" + UserGroupInformation.getLoginUser());
  }
}
