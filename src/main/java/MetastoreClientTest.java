import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Catalog;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.List;

public class MetastoreClientTest {

  private static final String KRB5_CONF = "KRB5_CONF";
  private static final String KRB5_PRINCIPAL = "KRB5_PRINCIPAL";
  private static final String KRB5_KEYTAB = "KRB5_KEYTAB";

  public static void main(String[] args) throws TException, IOException {
    Configuration conf = new Configuration();
    conf.set("hive.metastore.uris", "thrift://10.201.0.213:9093");
    conf.set("hive.metastore.sasl.enabled", "true");
    conf.set("hive.metastore.kerberos.principal", "hive-metastore/k8s-node1@HADOOP.COM");
    conf.set("hadoop.security.authentication", "kerberos");
//    conf.set("hadoop.rpc.protection", "integrity");
//    conf.set("hadoop.rpc.protection", "authentication");
    String krb5Conf = System.getenv(KRB5_CONF);
    String krb5Principal = System.getenv(KRB5_PRINCIPAL);
    String krb5Keytab = System.getenv(KRB5_KEYTAB);
    if (krb5Conf != null) {
      System.setProperty("java.security.krb5.conf", krb5Conf);
    }
    System.out.println("before login:" + UserGroupInformation.getLoginUser());
    System.out.println("krb5Conf:" + krb5Conf + ";principal:" + krb5Principal + ";keytab:" + krb5Keytab);
    long begin = System.currentTimeMillis();
    UserGroupInformation.setConfiguration(conf);
    UserGroupInformation.loginUserFromKeytab(krb5Principal, krb5Keytab);
    long end = System.currentTimeMillis();
    System.out.println("cost:" + (end-begin) + "ms");
    System.out.println("after login:" + UserGroupInformation.getLoginUser());

    begin = System.currentTimeMillis();
    System.out.println("before connect hms:");
    HiveMetaStoreClient metaStoreClient = new HiveMetaStoreClient(conf);
    System.out.println("connect cost:" + (System.currentTimeMillis()-begin) + "ms");
    List<String> dbs = metaStoreClient.getAllDatabases("hive");

    Catalog catalog = metaStoreClient.getCatalog("hive");

    Catalog newCatalog = new Catalog();
    newCatalog.setName("testcatalog");
    newCatalog.setLocationUri("/usr/hive3/warehouse");
    metaStoreClient.createCatalog(newCatalog);
    System.out.println("dbs:" + dbs);
  }
}
