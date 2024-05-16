import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.*;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlainMetastoreClientTest {

  public static void main(String[] args) throws TException, IOException {
    Configuration conf = new Configuration();

    conf.set("metastore.catalog.default", "lock_test_cl_765");
    conf.set("hive.metastore.uris", "thrift://10.201.0.250:30999");
//    conf.set("hive.metastore.uris", "thrift://10.201.0.250:30470");

    HiveMetaStoreClient metaStoreClient = new HiveMetaStoreClient(conf);

//    Table aaTable = metaStoreClient.getTable("aaa", "t_aa");
//    System.out.println(aaTable);

    String lockTableName = "t_aa3";

    List<LockComponent> components = new ArrayList<>();
    LockComponent lockComponent = new LockComponent(LockType.EXCLUSIVE, LockLevel.TABLE, "aaa");
    lockComponent.setTablename(lockTableName);
    components.add(lockComponent);
    String user = "aaa11";
    String hostname = "0.0.1.1";
    LockRequest lockRequest = new LockRequest(components, user, hostname);
    LockResponse lockResponse = metaStoreClient.lock(lockRequest);

    ShowLocksRequest showLocksRequest = new ShowLocksRequest();
    showLocksRequest.setDbname("aaa");
    showLocksRequest.setTablename(lockTableName);
//    ShowLocksResponse locksResponse = metaStoreClient.showLocks(showLocksRequest);
//    System.out.println(locksResponse);

//    Catalog newCatalog = new Catalog();
//    newCatalog.setName("testcatalog");
//    newCatalog.setLocationUri("/usr/hive3/warehouse");
//    metaStoreClient.createCatalog(newCatalog);
//    System.out.println("dbs:" + dbs);
  }
}
