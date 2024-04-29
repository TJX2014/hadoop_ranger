docker run -itd --net=host --privileged=true --name=kdc kdc bash

docker run -itd -p 89:88 -v kerberos:/var/kerberos --name=kdc1 kdc bash

kdb5_util create -r HADOOP.COM -s

start krb5kdc:
krb5kdc

addprinc -pw bigdata123 hdfs
ktadd -k /tmp/hdfs.keytab -norandkey hdfs@HADOOP.COM