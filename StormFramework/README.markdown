# Credits
This topology was inspired and built upon the work of the developers of the project "storm-kafka-stater" and the project "kafka-storm-starter", from Nathan Marz (https://github.com/nathanmarz) and Michael G Noll (https://github.com/miguno) respectivelly.


## Starting Kafka

I have included the Kafka 0.7.2 in a separate folder in the project folder in order to avoid you any trouble of finding a version of kafka that is 100% compatible with this project.

KAFKA FOLDER/bin/zookeeper-server-start.sh ../config/zookeeper.properties &
KAFKA FOLDER/bin/kafka-server-start.sh ../config/server.properties &c

### Testing Kafka Setup

./kafka-console-producer.sh -zookeeper localhost:2181 -topic test
Start providing messages in the terminal

Open a separate terminal window and:

./kafka-console-consumer.sh -zookeeper localhost:2181 -topic test -from-beginning
You should be able to get the messages

### Setting up Storm-Kafka 

IMPORTANT! : You MUST set up the ip address of your running kafka broker in the storm configuration file (StormFramework/src/jvm/storm/util/CONFIG_FILE.java) before running the project.

The topology KafkaTopology.java is already set up to monitor the load of several cassandra servers that publish their load to the kafka server I used to test the topology. You can change the address in order to watch your server in the config file.
    ------------------------------------
    Any kind of modification in the project can be made on the configuration file or in the KafkaTopology.java file. That includes the number of kafka topics you want to watch, the storm output folder, the IP addresses and so on...
    ------------------------------------

#### Run it using:
You can run the topology by using this command in the StormFramework folder:

mvn -f m2-pom.xml compile exec:java -Dexec.classpathScope=compile -Dexec.mainClass=storm.starter.KafkaTopology

or if you want to pack it all up in one JAR file:

mvn -f m2-pom.xml package
