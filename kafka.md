#Kafka linux 安装

wget https://dlcdn.apache.org/kafka/3.1.0/kafka_2.12-3.1.0.tgz。
tar -xf kafka_2.12-3.1.0.tgz 

启动： 
bin/zookeeper-server-start.sh -daemon config/zookeeper.properties  
bin/kafka-server-start.sh -daemon config/server.properties  

列出已有topic  
bin/kafka-topics.sh --list  --bootstrap-server localhost:9092  

新建topic  
bin/kafka-topics.sh --create --topic rawlog --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1  

产生消息客户端  
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic rawlog  

消费消息客户端  
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic rawlog  
