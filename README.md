# hadoop_ranger

powershell中ranger启动:
cd C:\Users\Allen\Workspace\apache\ranger\dev-support\ranger-docker
$env:RANGER_DB_TYPE="mysql"
docker-compose -f .\docker-compose.ranger-mysql.yml -f .\docker-compose.ranger.yml up -d

curl -XPOST "localhost:8081/jars/upload" -F "filename=@/tmp/flink-1.17.2-20481024-SNAPSHOT-session.jar"

curl -XPOST "www.1689460475067293699.baidu.com:32080/p232-s732/jars/upload" -F "filename=@/tmp/flink-1.17.2-20481024-SNAPSHOT-session.jar"