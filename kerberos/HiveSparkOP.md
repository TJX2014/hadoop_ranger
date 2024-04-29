db:
create database new_test2 location 'hdfs://k8s-node1:8020/hs2/new_test2';

table:
create table new_test2.aaa(a int, b string);

write data:
insert into new_test2.aaa values(22, 'bb'), (21, 'cc');
select * from new_test2.aaa;

table spec:
desc formatted new_test2.aaa;

spark fed:
client mode
$SPARK_HOME/bin/spark-sql --conf spark.driver.extraClassPath=/opt/apache-hive-3.1.2-bin/lib/hive-standalone-metastore-3.1.2.jar --conf spark.jars=local:///opt/spark-3.4.2-bin-hadoop3/iceberg-spark-runtime-3.4_2.12-1.4.0.jar --conf spark.hive.metastore.uris=thrift://k8s-node1:9093 --conf spark.sql.hive.metastore.jars=/opt/apache-hive-3.1.2-bin/lib/* --conf spark.sql.hive.metastore.version=3.1.2 --conf spark.kerberos.keytab=/tmp/xiaoxing.keytab --conf spark.kerberos.principal=xiaoxing/k8s-node1@HADOOP.COM --conf spark.hadoop.hadoop.security.authentication=kerberos --conf spark.hadoop.hadoop.security.authorization=true --conf spark.hadoop.yarn.resourcemanager.principal=xiaoxing/k8s-node1@HADOOP.COM --conf spark.hadoop.dfs.namenode.kerberos.principal=hdfs/_HOST@HADOOP.COM --conf spark.hadoop.dfs.data.transfer.protection=integrity --conf spark.hadoop.hive.metastore.sasl.enabled=true --conf spark.sql.catalog.spark_catalog.type=hive
--conf spark.master=local[*]
--conf spark.submit.deployMode=client

cluster mode
export KUBECONFIG=/root/k8sconfig/k8s117dev
--conf spark.submit.deployMode=client
--conf spark.master=k8s://https://...:6443
--conf spark.kubernetes.authenticate.driver.serviceAccountName=spark
--conf spark.kubernetes.container.image=.../spark:apache-v3.4.2
--conf spark.kubernetes.namespace=default