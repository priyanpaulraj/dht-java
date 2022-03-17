rm -f ./kill.sh
mvn clean package -Dmaven.test.skip=true
java -jar target/dht-0.0.1-SNAPSHOT.jar --server.address=localhost --server.port=8081 & echo 'kill -9 '$! >> kill.sh
sleep 5
java -jar target/dht-0.0.1-SNAPSHOT.jar --server.address=localhost --server.port=8082 --gateway.address=localhost --gateway.port=8081 & echo 'kill -9 '$! >> kill.sh
sleep 5
java -jar target/dht-0.0.1-SNAPSHOT.jar --server.address=localhost --server.port=8083 --gateway.address=localhost --gateway.port=8081 & echo 'kill -9 '$! >> kill.sh
echo 'echo killed all nodes' >> kill.sh