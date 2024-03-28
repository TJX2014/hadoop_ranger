登录kdc服务器
kadmin.local

创建hdfs账户指定密码为bigdata123
addprinc -pw bigdata123 hdfs/k8s-node1
addprinc -pw bigdata123 hive-metastore/k8s-node1
addprinc -pw bigdata123 yarn/k8s-node1
addprinc -pw bigdata123 xiaohong/k8s-node1

生成hdfs.keytab
ktadd -k /tmp/hdfs.keytab -norandkey hdfs/k8s-node1@HADOOP.COM
ktadd -k /tmp/hive-metastore.keytab -norandkey hive-metastore/k8s-node1@HADOOP.COM
ktadd -k /tmp/yarn.keytab -norandkey yarn/k8s-node1@HADOOP.COM
ktadd -k /tmp/xiaohong.keytab -norandkey xiaohong/k8s-node1@HADOOP.COM

kinit hive-metastore/k8s-node1@HADOOP.COM -kt /tmp/hive-metastore.keytab
kinit xiaohong/k8s-node1@HADOOP.COM -kt /tmp/xiaohong.keytab
kinit xiaoxing/k8s-node1@HADOOP.COM -kt /tmp/xiaoxing.keytab

格式化hdfs:
hdfs namenode -format

启动hdfs:
hadoop-daemon.sh start namenode
hadoop-daemon.sh start datanode

查看根目录hadoop默认acl
hdfs dfs -getfacl /

启动hive-metastore:
nohup hive --service metastore > /tmp/metastore.log &

启动ranger:
docker run -itd -v C:\usr\docker\ranger-2.4.0-admin\install.properties:/opt/ranger-2.1.1-admin/install.properties -v C:\Users\Allen\.m2\repository\com\mysql\mysql-connector-j\8.0.31\mysql-connector-j-8.0.31.jar:/mnt/c/Users/Allen/.m2/repository/com/mysql/mysql-connector-j/8.0.31/mysql-connector-j-8.0.31.jar -p 6080:6080 -p 5006:5005 --name=ranger deploy.deepexi.com/fastdata/ranger:157575

hdfs官方安装包开启ranger-hdfs缺少的包:
scp /mnt/c/Users/Allen/.m2/repository/org/apache/ranger/ranger-hdfs-plugin/2.4.0/ranger-hdfs-plugin-2.4.0.jar  k8s-node1:/opt/hadoop-3.1.1/share/hadoop/hdfs/
scp /mnt/c/Users/Allen/.m2/repository/org/apache/ranger/ranger-plugins-common/2.4.0/ranger-plugins-common-2.4.0.jar  k8s-node1:/opt/hadoop-3.1.1/share/hadoop/hdfs/
scp /mnt/c/Users/Allen/.m2/repository/org/apache/ranger/ranger-plugins-audit/2.4.0/ranger-plugins-audit-2.4.0.jar  k8s-node1:/opt/hadoop-3.1.1/share/hadoop/hdfs/
scp /mnt/c/Users/Allen/.m2/repository/com/kstruct/gethostname4j/1.0.0/gethostname4j-1.0.0.jar  k8s-node1:/opt/hadoop-3.1.1/share/hadoop/hdfs/
scp /mnt/c/Users/Allen/.m2/repository/net/java/dev/jna/jna/5.7.0/jna-5.7.0.jar  k8s-node1:/opt/hadoop-3.1.1/share/hadoop/hdfs/