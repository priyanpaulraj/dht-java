rm -f ./kill.sh
mvn clean package -Dmaven.test.skip=true
port=${1:-8081}
java -jar target/dht-0.0.1-SNAPSHOT.jar --server.address=localhost --server.port=$port &
echo 'kill -9 '$! >>kill.sh
no_of_nodes=${2:-0}
node_port=$port
while [ $no_of_nodes -gt 1 ]; do
    sleep 5
    ((node_port++))
    java -jar target/dht-0.0.1-SNAPSHOT.jar --server.address=localhost --server.port=$node_port --gateway.address=localhost --gateway.port=$port &
    echo 'kill -9 '$! >>kill.sh
    ((no_of_nodes--))
done
echo 'echo killed all nodes' >>kill.sh
