package org.apache.spark;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.util.List;

public class HiveCatalogTest {

  public static void main(String[] args) throws IOException {
    String krbFile = "/tmp/krb5.conf";
    System.setProperty("java.security.krb5.conf", krbFile);

    Configuration conf = new Configuration();
    conf.set("hadoop.security.authentication", UserGroupInformation.AuthenticationMethod.KERBEROS.toString());
    conf.set("ipc.client.fallback-to-simple-auth-allowed", "true");
    UserGroupInformation.setConfiguration(conf);
    UserGroupInformation.loginUserFromKeytab("xiaoxing", "/tmp/xiaoxing.keytab");

    Path p = new Path("hdfs://node01:8020/usr/hive2/warehouse/test13.db/aa2");
    FileSystem fileSystem = p.getFileSystem(conf);
    FileStatus[] fileStatuses = fileSystem.listStatus(p);

    SparkSession.Builder builder = SparkSession.builder().appName("hive_crud");
    builder.config("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
    builder.config("spark.kryoserializer.buffer.max", "512m");
    builder.config("spark.driver.host", "0.0.0.0");

    builder.config("spark.hive.metastore.uris", "thrift://node02:9093");
    builder.config("spark.hive.metastore.sasl.enabled", "true");
    builder.config("spark.hive.metastore.kerberos.principal", "hdfs/node02@DEEPEXI.COM");
    System.setProperty("spark.hadoop.hadoop.security.authentication", "kerberos");

    builder.config("spark.sql.catalog.hiveaaa", "org.apache.hive.HiveCatalog");
    builder.config("spark.sql.catalog.hiveaaa.type", "hive");
    builder.config("spark.sql.catalog.hiveaaa.metastore.catalog.default", "hive");
    builder.config("spark.sql.catalog.hiveaaa.hadoop.security.authentication", "kerberos");
    builder.config("spark.sql.catalog.hiveaaa.hive.metastore.uris", "thrift://node02:9093");
    builder.config("spark.sql.catalog.hiveaaa.metastore.sasl.enabled", "true");
    builder.config("spark.sql.catalog.hiveaaa.metastore.kerberos.principal", "hdfs/node02@DEEPEXI.COM");

    builder.master("local[*]");
    builder.enableHiveSupport();
    SparkSession spark = builder.getOrCreate();

    List dbs = spark.sql("show databases").collectAsList();

//    spark.sql("insert into spark_catalog.test13.aa2 values(11, 'aa')");

    spark.sql("use hiveaaa");

    List catalogs = spark.sql("show catalogs").collectAsList();
    List res = spark.sql("show databases").collectAsList();

//    spark.sql("create table hiveaaa.test13.aa31(a string)");

    spark.sql("insert into hiveaaa.test13.aa31 values('-11')");

    List results = spark.sql("select * from hiveaaa.test13.aa31").collectAsList();
    System.out.println("catalogs:" + catalogs);
    System.out.println(res);
  }
}
