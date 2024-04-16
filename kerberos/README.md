登录kdc服务器
kadmin.local

创建hdfs账户指定密码为bigdata123
addprinc -pw bigdata123 hdfs/k8s-node1
addprinc -pw bigdata123 hive-metastore/k8s-node1
addprinc -pw bigdata123 yarn/k8s-node1
addprinc -pw bigdata123 xiaohong/k8s-node1
addprinc -pw bigdata123 hive-server2/k8s-node1
addprinc -pw bigdata123 hive-server2/k8s-node3
addprinc -pw bigdata123 hive-server2/k8s_local
addprinc -pw bigdata123 zookeeper/k8s-node1
addprinc -pw bigdata123 admin/k8s-node1
addprinc -pw bigdata123 xiaoxing/k8s-node3

生成hdfs.keytab
ktadd -k /tmp/hdfs.keytab -norandkey hdfs/k8s-node1@HADOOP.COM
ktadd -k /tmp/hive-metastore.keytab -norandkey hive-metastore/k8s-node1@HADOOP.COM
ktadd -k /tmp/yarn.keytab -norandkey yarn/k8s-node1@HADOOP.COM
ktadd -k /tmp/xiaohong.keytab -norandkey xiaohong/k8s-node1@HADOOP.COM
ktadd -k /tmp/hive-server2.keytab -norandkey hive-server2/k8s-node1 hive-server2/k8s-node3
ktadd -k /tmp/hive-server2.keytab -norandkey hive-server2/k8s_local hive-server2/k8s-node1 hive-server2/k8s-node3
ktadd -k /tmp/zookeeper.keytab -norandkey zookeeper/k8s-node1@HADOOP.COM
ktadd -k /tmp/admin.keytab -norandkey admin/k8s-node1@HADOOP.COM
ktadd -k /tmp/xiaoxing-node3.keytab -norandkey xiaoxing/k8s-node3@HADOOP.COM

kinit hive-metastore/k8s-node1@HADOOP.COM -kt /tmp/hive-metastore.keytab
kinit xiaohong/k8s-node1@HADOOP.COM -kt /tmp/xiaohong.keytab
kinit xiaoxing/k8s-node1@HADOOP.COM -kt /tmp/xiaoxing.keytab

格式化hdfs:
hdfs namenode -format

启动hdfs:
hadoop-daemon.sh start namenode
hadoop-daemon.sh start datanode

nohup java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -cp `hadoop classpath` o
rg.apache.hadoop.hdfs.server.namenode.NameNode > /tmp/nn.log &

查看根目录hadoop默认acl
hdfs dfs -getfacl /

启动zookeeper:
cd $ZOOKEEPER_HOME
server:
nohup java -Djava.security.auth.login.config=$ZOOKEEPER_HOME/conf/zookeeper_jaas.conf -cp "$ZOOKEEPER_HOME/lib/*" org.apache.zookeeper.server.quorum.QuorumPeerMain conf/zoo.cfg > /tmp/zk.log &
client:
java -Djava.security.auth.login.config=$ZOOKEEPER_HOME/conf/zookeeper_jaas.conf -Dzookeeper.server.principal=zookeeper/k8s-node1@HADOOP.COM -cp "$ZOOKEEPER_HOME/lib/*" org.apache.zookeeper.ZooKeeperMain

启动hive
hive-metastore:
nohup hive --service metastore > /tmp/metastore.log &
nohup java -cp `hadoop classpath`:$HIVE_HOME/conf:$HIVE_HOME/lib/* org.apache.hadoop.hive.metastore.HiveMetaStore > /tmp/hms.log &
hive-server2:
nohup java -Djava.library.path=$HADOOP_HOME/lib/native -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 -cp $HIVE_HOME/conf:$HIVE_HOME/lib/*:`hadoop classpath` org.apache.hive.service.server.HiveServer2 > /tmp/hs2.log &

启动ranger:
docker run -itd -v C:\usr\docker\ranger-2.4.0-admin\install.properties:/opt/ranger-2.1.1-admin/install.properties -v C:\Users\Allen\.m2\repository\com\mysql\mysql-connector-j\8.0.31\mysql-connector-j-8.0.31.jar:/mnt/c/Users/Allen/.m2/repository/com/mysql/mysql-connector-j/8.0.31/mysql-connector-j-8.0.31.jar -p 6080:6080 -p 5006:5005 --name=ranger deploy.deepexi.com/fastdata/ranger:157575

hdfs官方安装包开启ranger-hdfs缺少的包:
scp /mnt/c/Users/Allen/.m2/repository/org/apache/ranger/ranger-hdfs-plugin/2.4.0/ranger-hdfs-plugin-2.4.0.jar  k8s-node1:/opt/hadoop-3.1.1/share/hadoop/hdfs/
scp /mnt/c/Users/Allen/.m2/repository/org/apache/ranger/ranger-plugins-common/2.4.0/ranger-plugins-common-2.4.0.jar  k8s-node1:/opt/hadoop-3.1.1/share/hadoop/hdfs/
scp /mnt/c/Users/Allen/.m2/repository/org/apache/ranger/ranger-plugins-audit/2.4.0/ranger-plugins-audit-2.4.0.jar  k8s-node1:/opt/hadoop-3.1.1/share/hadoop/hdfs/
scp /mnt/c/Users/Allen/.m2/repository/com/kstruct/gethostname4j/1.0.0/gethostname4j-1.0.0.jar  k8s-node1:/opt/hadoop-3.1.1/share/hadoop/hdfs/
scp /mnt/c/Users/Allen/.m2/repository/net/java/dev/jna/jna/5.7.0/jna-5.7.0.jar  k8s-node1:/opt/hadoop-3.1.1/share/hadoop/hdfs/

scp /mnt/c/Users/Allen/.m2/repository/org/apache/ranger/ranger-hive-plugin/2.4.0/ranger-hive-plugin-2.4.0.jar k8s-node1:/opt/apache-hive-3.1.2-bin/lib

beeline客户端
kinit
kinit xiaoxing/k8s-node1@HADOOP.COM -kt /tmp/xiaoxing.keytab
beeline -u "jdbc:hive2://k8s-node1:10000/test1;principal=hive-server2/k8s-node1@HADOOP.COM"
beeline -u "jdbc:hive2://k8s-node1:10000/test1;principal=hive-server2/_HOST@HADOOP.COM"
direct:
beeline -u "jdbc:hive2://k8s-node1:10000/;principal=hive-server2/_HOST@HADOOP.COM"
java -cp `hadoop classpath`:$HIVE_HOME/conf:$HIVE_HOME/lib/*:/opt/apache-hive-3.1.2-bin/lib/hive-beeline-3.1.2.jar org.apache.hive.beeline.BeeLine -u "jdbc:hive2://k8s-node1:10000/test1;principal=hive-server2/_HOST@HADOOP.COM"
by zk:
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Djava.security.auth.login.config=$ZOOKEEPER_HOME/conf/zookeeper_jaas.conf -Dzookeeper.server.principal=zookeeper/k8s-node1@HADOOP.COM -cp `hadoop classpath`:$HIVE_HOME/conf:$HIVE_HOME/lib/*:/opt/apache-hive-3.1.2-bin/lib/hive-beeline-3.1.2.jar org.apache.hive.beeline.BeeLine -u "jdbc:hive2://k8s-node1:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2" -n xiaoxing

spark客户端:
unset HIVE_HOME HIVE_CONF_DIR HADOOP_HOME

hive catalog:
bin/spark-sql --conf spark.jars=local:///opt/spark-3.4.2-bin-hadoop3/iceberg-spark-runtime-3.4_2.12-1.4.0.jar --conf spark.submit.deployMode=client --conf spark.master=local[*] --conf spark.hive.metastore.uris=thrift://0.0.0.0:9093 --conf spark.sql.defaultCatalog=spark_catalog --conf spark.sql.hive.metastore.jars=/opt/apache-hive-3.1.2-bin/lib/* --conf spark.sql.hive.metastore.version=3.1.2 --conf spark.kerberos.keytab=/tmp/xiaoxing.keytab --conf spark.kerberos.principal=xiaoxing/k8s-node1@HADOOP.COM --conf spark.hadoop.hadoop.security.authentication=kerberos --conf spark.hadoop.hadoop.security.authorization=true --conf spark.hadoop.yarn.resourcemanager.principal=xiaoxing/k8s-node1@HADOOP.COM --conf spark.hadoop.dfs.namenode.kerberos.principal=hdfs/_HOST@HADOOP.COM --conf spark.hadoop.dfs.data.transfer.protection=integrity

iceberg catalog:
bin/spark-sql --conf spark.jars=local:///opt/spark-3.4.2-bin-hadoop3/iceberg-spark-runtime-3.4_2.12-1.4.0.jar --conf spark.submit.deployMode=client --conf spark.master=local[*] --conf spark.hive.metastore.uris=thrift://0.0.0.0:9093 --conf spark.sql.defaultCatalog=spark_catalog --conf spark.sql.hive.metastore.jars=/opt/apache-hive-3.1.2-bin/lib/* --conf spark.sql.hive.metastore.version=3.1.2 --conf spark.kerberos.keytab=/tmp/xiaoxing.keytab --conf spark.kerberos.principal=xiaoxing/k8s-node1@HADOOP.COM --conf spark.hadoop.hadoop.security.authentication=kerberos --conf spark.hadoop.hadoop.security.authorization=true --conf spark.hadoop.yarn.resourcemanager.principal=xiaoxing/k8s-node1@HADOOP.COM --conf spark.hadoop.dfs.namenode.kerberos.principal=hdfs/_HOST@HADOOP.COM --conf spark.hadoop.dfs.data.transfer.protection=integrity

bin/spark-sql --conf spark.jars=local:///opt/spark-3.4.2-bin-hadoop3/iceberg-spark-runtime-3.4_2.12-1.4.0.jar --conf spark.submit.deployMode=client --conf spark.master=local[*] --conf spark.hive.metastore.uris=thrift://0.0.0.0:9093 --conf spark.sql.defaultCatalog=spark_catalog --conf spark.sql.hive.metastore.jars=/opt/apache-hive-3.1.2-bin/lib/* --conf spark.sql.hive.metastore.version=3.1.2 --conf spark.kerberos.keytab=/tmp/xiaoxing.keytab --conf spark.kerberos.principal=xiaoxing/k8s-node1@HADOOP.COM --conf spark.hadoop.hadoop.security.authentication=kerberos --conf spark.hadoop.hadoop.security.authorization=true --conf spark.hadoop.yarn.resourcemanager.principal=xiaoxing/k8s-node1@HADOOP.COM --conf spark.hadoop.dfs.namenode.kerberos.principal=hdfs/_HOST@HADOOP.COM --conf spark.hadoop.dfs.data.transfer.protection=integrity --conf spark.sql.extensions=org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions --conf spark.sql.catalog.spark_catalog=org.apache.iceberg.spark.SparkSessionCatalog --conf spark.sql.catalog.spark_catalog.type=hive --conf spark.sql.catalog.local=org.apache.iceberg.spark.SparkCatalog --conf spark.sql.catalog.local.type=hive --conf spark.sql.catalog.local.uri=thrift://10.201.0.250:30470 --conf spark.sql.catalog.local.metastore.catalog.default=xiaoxing_ice5_708 --conf spark.driver.extraJavaOptions=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5007

spark用hive3自定义catalog名查询:
bin/spark-sql --conf spark.driver.extraClassPath=/opt/apache-hive-3.1.2-bin/lib/hive-standalone-metastore-3.1.2.jar --conf spark.jars=local:///opt/spark-3.4.2-bin-hadoop3/iceberg-spark-runtime-3.4_2.12-1.4.0.jar --conf spark.submit.deployMode=client --conf spark.master=local[*] --conf spark.hive.metastore.uris=thrift://0.0.0.0:9093 --conf spark.sql.defaultCatalog=local --conf spark.sql.hive.metastore.jars=/opt/apache-hive-3.1.2-bin/lib/* --conf spark.sql.hive.metastore.version=3.1.2 --conf spark.kerberos.keytab=/tmp/xiaoxing.keytab --conf spark.kerberos.principal=xiaoxing/k8s-node1@HADOOP.COM --conf spark.hadoop.hadoop.security.authentication=kerberos --conf spark.hadoop.hadoop.security.authorization=true --conf spark.hadoop.yarn.resourcemanager.principal=xiaoxing/k8s-node1@HADOOP.COM --conf spark.hadoop.dfs.namenode.kerberos.principal=hdfs/_HOST@HADOOP.COM --conf spark.hadoop.dfs.data.transfer.protection=integrity --conf spark.sql.catalog.spark_catalog.type=hive --conf spark.sql.catalog.local=org.apache.iceberg.spark.SparkCatalog --conf spark.sql.catalog.local.type=hive --conf spark.sql.catalog.local.hive.metastore.sasl.enabled=false --conf spark.sql.catalog.local.uri=thrift://10.201.0.250:30470 --conf spark.sql.catalog.local.hadoop.metastore.catalog.default=xiaoxing_ice5_708 --conf spark.driver.extraJavaOptions=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5007

--conf spark.driver.extraJavaOptions=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
bin/spark-sql --conf spark.jars=local:///opt/spark-3.4.2-bin-hadoop3/iceberg-spark-runtime-3.4_2.12-1.4.0.jar --conf spark.submit.deployMode=client --conf spark.master=local[*] --conf spark.sql.extensions=org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions 