To run:

As independent Node:
    
     java -jar target/dht-0.0.1-SNAPSHOT.jar --server.address=localhost --server.port=8081

And add to existing cluster :
    
    java -jar target/dht-0.0.1-SNAPSHOT.jar --server.address=localhost --server.port=8082 --gateway.address=localhost --gateway.port=8081