import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore;
import org.apache.hadoop.hive.metastore.security.HadoopThriftAuthBridge;
import org.apache.hadoop.hive.metastore.utils.MetaStoreUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LowLevelHMSThriftClient {

  private static final String KRB5_CONF = "KRB5_CONF";
  private static final String KRB5_PRINCIPAL = "KRB5_PRINCIPAL";
  private static final String KRB5_KEYTAB = "KRB5_KEYTAB";

  public static void main(String[] args) throws IOException, TException {
    Configuration conf = new Configuration();
//    conf.set("hadoop.rpc.protection", "integrity");
//    conf.set("hadoop.rpc.protection", "authentication");
    conf.set("hadoop.security.authentication", "kerberos");
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
    System.out.println("after login:" + UserGroupInformation.getLoginUser());

    HiveConf hiveConf = new HiveConf();
    hiveConf.set("hive.metastore.kerberos.principal", "hive/_HOST@DEEPEXI.COM");
    hiveConf.set("hive.metastore.uris", "thrift://node01:9083");
    hiveConf.set("hive.metastore.sasl.enabled", "true");
    HiveMetaStoreClient metaStoreClient = new HiveMetaStoreClient(hiveConf);

    HadoopThriftAuthBridge.Client authBridge = HadoopThriftAuthBridge.getBridge().createClient();
    String principalConfig = "hive/_HOST@DEEPEXI.COM";
    String host = "node01";
    TTransport transport = new TSocket(host, 9083, 600_000);
    final Map<String, String> saslProps = new HashMap<>();
    saslProps.put("javax.security.sasl.qop", "auth");
    saslProps.put("javax.security.sasl.server.authentication", "true");
    transport = authBridge.createClientTransport(principalConfig, host, "KERBEROS", (String)null, transport, saslProps);
    TProtocol protocol = new TBinaryProtocol(transport);
//    TProtocol protocol = new TCompactProtocol(transport);
    ThriftHiveMetastore.Client client = new ThriftHiveMetastore.Client(protocol);
    if (!transport.isOpen()) {
      transport.open();
    }
//    client.get_da
    List<String> dbs1 = client.get_all_databases();
    System.out.println(dbs1);
//    List<String> dbs = metaStoreClient.getAllDatabases();
//    System.out.println(dbs);
  }
}
