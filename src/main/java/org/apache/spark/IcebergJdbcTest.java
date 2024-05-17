package org.apache.spark;

import org.apache.spark.sql.SparkSession;

import java.util.List;

public class IcebergJdbcTest {

  public static void main(String[] args) {
    SparkSession.Builder builder = SparkSession.builder().appName("hudi_crud");
    builder.config("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
//        builder.config("spark.kryo.registrator", "org.apache.spark.HoodieSparkKryoRegistrar");
    builder.config("spark.kryoserializer.buffer.max", "512m");
    builder.config("spark.driver.host", "0.0.0.0");

//        builder.config("spark.sql.catalog.iceberg_catalog", "org.apache.iceberg.spark.SparkSessionCatalog");

//        builder.config("spark.sql.catalog.iceberg_catalog", "org.apache.iceberg.spark.SparkCatalog");
//        builder.config("spark.sql.catalog.iceberg_catalog.type", "hive");
//        builder.config("spark.sql.catalog.iceberg_catalog.hadoop.metastore.catalog.default", "hdfs104_629");
//        builder.config("spark.sql.catalog.iceberg_catalog.uri", "thrift://10.201.0.212:30470");

    builder.config("spark.sql.catalog.iceberg_dev_proxy", "org.apache.iceberg.spark.SparkCatalog");
    builder.config("spark.sql.catalog.iceberg_dev_proxy.type", "hive");
    builder.config("spark.sql.catalog.iceberg_dev_proxy.hadoop.metastore.catalog.default", "hdfs104_629");
    builder.config("spark.sql.catalog.iceberg_dev_proxy.uri", "thrift://10.201.0.192:30999");

    builder.config("spark.sql.catalog.jdbc_catalog", "org.apache.spark.sql.execution.datasources.v2.jdbc.JDBCTableCatalog");
    builder.config("spark.sql.catalog.jdbc_catalog.url", "jdbc:mysql://10.201.0.26:13306");
    builder.config("spark.sql.catalog.jdbc_catalog.user", "root");
    builder.config("spark.sql.catalog.jdbc_catalog.password", "123456");

//        org.apache.spark.sql.internal.SQLConf sqlConf = new org.apache.spark.sql.internal.SQLConf();
//        sqlConf.setConfString("spark.sql.catalog.iceberg_catalog", "org.apache.iceberg.spark.SparkCatalog");
//        sqlConf.setConfString("spark.sql.catalog.iceberg_catalog.type", "hive");
//        sqlConf.setConfString("spark.sql.catalog.iceberg_catalog.hadoop.metastore.catalog.default", "hdfs104_629");
//        sqlConf.setConfString("spark.sql.catalog.iceberg_catalog.uri", "thrift://10.201.0.212:30470");
//        CatalogPlugin icebergCatalog = Catalogs.load("iceberg_catalog", sqlConf);
//        System.out.println(icebergCatalog);

//        builder.config("spark.sql.warehouse.dir", WAREHOUSE);
    builder.master("local[*]");
    builder.enableHiveSupport();
    SparkSession spark = builder.getOrCreate();

    spark.sql("use iceberg_dev_proxy");
//        spark.sql("use iceberg_catalog");

//        spark.sql("select * \n" +
//                "from iceberg_catalog.ns1.t1").collectAsList();
    List catalogs = spark.sql("show catalogs").collectAsList();
    List res = spark.sql("show databases").collectAsList();
    System.out.println("catalogs:" + catalogs);
    System.out.println(res);

    System.out.println("tables from iceberg_dev_proxy:" + spark.sql("show tables from iceberg_dev_proxy.dlink_a").collectAsList());

    System.out.println("create table:" + spark.sql("create table iceberg_dev_proxy.dlink_a.aaa(a int, b string)").collectAsList());
  }
}
