# hadoop_ranger

powershell中ranger启动:
cd C:\Users\Allen\Workspace\apache\ranger\dev-support\ranger-docker
$env:RANGER_DB_TYPE="mysql"
docker-compose -f .\docker-compose.ranger-mysql.yml -f .\docker-compose.ranger.yml up -d