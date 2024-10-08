bin/spark-sql --conf spark.jars=local:///opt/spark-3.4.2-bin-hadoop3/iceberg-spark-runtime-3.4_2.12-1.4.0.jar --conf spark.submit.deployMode=client --conf spark.master=local[*] --conf spark.hive.metastore.uris=thrift://0.0.0.0:9093 --conf spark.sql.defaultCatalog=spark_catalog --conf spark.sql.hive.metastore.jars=/opt/apache-hive-3.1.2-bin/lib/* --conf spark.kerberos.keytab=/tmp/xiaoxing.keytab --conf spark.kerberos.principal=xiaoxing/k8s-node1@HADOOP.COM --conf spark.hadoop.hadoop.security.authentication=kerberos --conf spark.hadoop.hadoop.security.authorization=true --conf spark.hadoop.yarn.resourcemanager.principal=xiaoxing/k8s-node1@HADOOP.COM --conf spark.hadoop.dfs.namenode.kerberos.principal=hdfs/_HOST@HADOOP.COM --conf spark.hadoop.dfs.data.transfer.protection=integrity --conf spark.sql.catalog.xiaoxing_ice5_708=org.apache.iceberg.spark.SparkCatalog --conf spark.sql.catalog.xiaoxing_ice5_708.uris=thrift://...:30470 --conf spark.sql.extensions=org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions 

bin/spark-sql --conf spark.jars=local:///opt/spark-3.4.2-bin-hadoop3/iceberg-spark-runtime-3.4_2.12-1.4.0.jar --conf spark.submit.deployMode=client --conf spark.master=local[*] --conf spark.hive.metastore.uris=thrift://...:30999 --conf spark.sql.defaultCatalog=spark_catalog --conf spark.sql.catalog.spark_catalog=org.apache.iceberg.spark.SparkCatalog --conf spark.sql.extensions=org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions --conf spark.hadoop.hive.metastore.sasl.enabled=false --conf spark.hadoop.dfs.nameservices=nameservice1 --conf spark.hadoop.dfs.client.failover.proxy.provider.nameservice1=org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider --conf spark.hadoop.dfs.ha.namenodes.nameservice1=namenode53,namenode82 --conf spark.hadoop.dfs.namenode.rpc-address.nameservice1.namenode53=dcmp8:8020 --conf spark.hadoop.dfs.namenode.rpc-address.nameservice1.namenode82=dcmp9:8020 --conf spark.driver.extraJavaOptions=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5007 

docker run --name=jdk11-dev -p 8080:8080 -p 8081:8081 -p 4040:4040 -p 7077:7077 -e SPARK_HOME=/root/Software/spark-3.2.4-bin-hadoop3.2 -itd -v /tmp:/tmp -v /Users/admin/Software:/root/Software -v /Users/admin/Projects:/root/Projects public.ecr.aws/docker/library/openjdk:11 bash

docker run --name=jdk11-dev -p 8080:8080 -p 8081:8081 -p 4040:4040 -p 7077:7077 -e SPARK_HOME=/Users/admin/Software/spark-3.2.4-bin-hadoop3.2 -itd -v /tmp:/tmp -v /Users/admin/Software:/Users/admin/Software -v /Users/admin/Projects:/root/Projects public.ecr.aws/docker/library/openjdk:11 bash

cd blaze/tpcds/benchmark-runner
./bin/run --data-location ~/Software/tpcds-20 --output-dir ./benchmark-result --query-filter q23a

,q23b,q64,q80

/Users/admin/Library/Java/JavaVirtualMachines/azul-1.8.0_382/Contents/Home/bin/java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005 -cp /Users/admin/Software/spark-3.2.4-bin-hadoop3.2/conf/:/Users/admin/Software/spark-3.2.4-bin-hadoop3.2/jars/* -Xmx1g org.apache.spark.deploy.SparkSubmit --class org.apache.spark.sql.execution.benchmark.TPCDSBenchmarkRunner ./bin/../target/tpcds-benchmark-0.1.0-SNAPSHOT-with-dependencies.jar --data-location /Users/admin/Software/tpcds-20 --output-dir ./benchmark-result --query-filter q23a

val tables: Seq[String] = Seq("catalog_page", "catalog_returns", "customer", "customer_address",
      "customer_demographics", "date_dim", "household_demographics", "inventory", "item",
      "promotion", "store", "store_returns", "catalog_sales", "web_sales", "store_sales",
      "web_returns", "web_site", "reason", "call_center", "warehouse", "ship_mode", "income_band",
      "time_dim", "web_page")

val dataLocation = "/Users/admin/Software/tpcds-20"

tables.par.foreach { tableName =>
    spark.read.parquet(s"$dataLocation/$tableName").createOrReplaceTempView(tableName)
}

tableName -> spark.table(tableName).count()

val queryResource = s"file:///Users/admin/Projects/blaze/tpcds/benchmark-runner/src/main/resources/tpcds/queries/q23a.sql"
val resourceUrl = new java.net.URL(queryResource)
val queryString = {
    org.apache.commons.io.IOUtils.toString(resourceUrl)
}

val begin = System.currentTimeMillis()
spark.sql(queryString).show
