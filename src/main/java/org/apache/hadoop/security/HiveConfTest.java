package org.apache.hadoop.security;

import org.apache.hadoop.EnvUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.ObjectStore;
import org.apache.hadoop.hive.metastore.api.Catalog;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;

import java.util.HashMap;
import java.util.Map;

public class HiveConfTest {

  public static void main(String[] args) throws MetaException, NoSuchObjectException {
    Map<String, String> env = new HashMap<>();
    env.put("HIVE_CONF_DIR", "/usr/local/metastore/conf");
    env.put("HIVE_HOME", "/usr/local/metastore");
    EnvUtils.setEnv(env, false);
    HiveConf hiveConf = new HiveConf();
    String dir = hiveConf.get("metastore.warehouse.dir");
    System.out.println(dir);

    ObjectStore objectStore = new ObjectStore();
    objectStore.setConf(hiveConf);
    String dbSchemaVer = objectStore.getMetaStoreSchemaVersion();
    Catalog catalog = objectStore.getCatalog("hive");
    System.out.println(catalog);
  }
}
