# kafkaAssignment

Technology used

Java 1.8

MVN: 3.3.9

Kafka 3.0.0

Confluent 7.3.1

IntelliJ IDEA - 2017.2.5



1. Start the Zookeeper server

patturaj@N-20N3PF1CDCX7:/usr/lib/kafka_2.12-3.0.0$ ./bin/zookeeper-server-start.sh config/zookeeper.properties

2. Start the Kafka Broker Cluster

patturaj@N-20N3PF1CDCX7:/usr/lib/kafka_2.12-3.0.0$ ./bin/kafka-server-start.sh config/server.properties

3. Create a topic

patturaj@N-20N3PF1CDCX7:/usr/lib/kafka_2.12-3.0.0$ ./bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic streams-json-input
Created topic streams-json-input.

patturaj@N-20N3PF1CDCX7:/usr/lib/kafka_2.12-3.0.0$ ./bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic streams-most-frequent-alarms
Created topic streams-most-frequent-alarms.

patturaj@N-20N3PF1CDCX7:/usr/lib/kafka_2.12-3.0.0$ ./bin/kafka-topics.sh --list --bootstrap-server localhost:9092                                        
streams-ERA015-alarms-per-hour
streams-json-input
streams-most-frequent-alarms


4. Start a producer

patturaj@N-20N3PF1CDCX7:cat /mnt/c/Users/patturaj/Desktop/Ramkumar/Personal/Documents/Information_is_Wealth/Job_Searching_2022/Elisa_Kafka/src/main/java/package.json | ./bin/kafka-console-producer.sh --broker-list localhost:9092 --topic streams-json-input


5. Start a consumer

patturaj@N-20N3PF1CDCX7:/usr/lib/kafka_2.12-3.0.0$ ./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic streams-most-frequent-alarms --from-beginning

6. Start Confluent Server

patturaj@N-20N3PF1CDCX7: confluent local services start

Statistics Data produced in outputAlarmStatistics.txt
