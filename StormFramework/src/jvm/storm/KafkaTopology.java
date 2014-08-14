/*
 * Kafka Topology
 * Topology to monitor and rank the load of Geo Distributed servers that publish their status or any message in the format [(STRING),(LONG)] to a Kafka server.
 */
package storm;

import java.util.ArrayList;
import java.util.List;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;


import backtype.storm.task.ShellBolt;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import storm.kafka.KafkaConfig.StaticHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;

import java.util.HashMap;
import java.util.Map;

import storm.bolt.PreliminaryRankingBolt;
import storm.bolt.RollingCountBolt;
import storm.bolt.TopicRankingBolt;
import storm.bolt.KafkaMessageParser;
import storm.bolt.GlobalRankingsBolt;
import storm.util.StormRunner;
import storm.util.CONFIG_FILE;


public class KafkaTopology {
    
    //Maximum number of servers that will be monitored.
    final static int TOP_N = CONFIG_FILE.TOP_N;
    //How many seconds in the past should the system keep information about the servers.
    final static int slidingWindowSize = CONFIG_FILE.slidingWindowSize;
    //The emit frequency from a Bolt to another.
    final static int emitFrequencyInSeconds = CONFIG_FILE.emitFrequencyInSeconds;
    
    
    public static void main(String[] args) throws Exception {
        
        TopologyBuilder builder = createKafkaTopology();
        
        Config config = new Config();
        config.setDebug(true);
        
        try{
            StormRunner.runTopologyLocally(builder.createTopology(), CONFIG_FILE.topologyName, config, 0);
        }
        catch (InterruptedException e){
            System.out.println("\n\n Execution interrupted. \n\n");
        }
    }
    
    static private TopologyBuilder createKafkaTopology(){
        
        TopologyBuilder topology = new TopologyBuilder();
        //Creates and connects all the bolts for each topic.
        for (CONFIG_FILE.Topics topic: CONFIG_FILE.Topics.values())
            createKafkaTopicRankingTopology (topology, CONFIG_FILE.kafkaBrokerIp, topic.toString());
        //Connects each one of the topics to the global ranker bolt.
        createKafkaGlobalRankingTopology(topology);
        return topology;
    }
    
    static private void createKafkaGlobalRankingTopology(TopologyBuilder builder){
        
        
        GlobalRankingsBolt globalRankingsBolt = new GlobalRankingsBolt(slidingWindowSize,emitFrequencyInSeconds);
        BoltDeclarer bolt = builder.setBolt(CONFIG_FILE.globalRankingsBoltName, globalRankingsBolt);
        for (CONFIG_FILE.Topics topic: CONFIG_FILE.Topics.values()){
            bolt = bolt.fieldsGrouping(topic.toString() + CONFIG_FILE.topicRankerBoltName, new Fields(CONFIG_FILE.fieldTopicRankingBoltOutput));
            
        }
    }
    
    static private void createKafkaTopicRankingTopology(TopologyBuilder builder, String kafkaHostnameAndPort, String topic){
        
        String spoutId = topic + CONFIG_FILE.kafkaSpoutName;
        String parserId = topic + CONFIG_FILE.kafkaMessageParserBoltName;
        String counterId = topic + CONFIG_FILE.rollingCountBoltName;
        String preliminaryRankerId = topic + CONFIG_FILE.combinerBoltName;
        String topicRankerId = topic + CONFIG_FILE.topicRankerBoltName;
        
        //Kafka Spout Config
        SpoutConfig kafkaConf = new SpoutConfig(new SpoutConfig.ZkHosts(kafkaHostnameAndPort,CONFIG_FILE.brokers),
                                                topic, // topic to read from
                                                CONFIG_FILE.zookeperRootPath, // the root path in Zookeeper for the spout to store the consumer offsets
                                                topic+CONFIG_FILE.consumerID); // an id for this consumer for storing the consumer offsets in Zookeeper
        
        kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());
        KafkaSpout kafkaSpout = new KafkaSpout(kafkaConf);
        
        //Set the Spout
        
        builder.setSpout(spoutId, kafkaSpout, 1);
        
        //Set and Connect the Bolts for a Topic.
        
        builder.setBolt(parserId, new KafkaMessageParser(), 1).fieldsGrouping(spoutId, kafkaConf.scheme.getOutputFields());
        builder.setBolt(counterId, new RollingCountBolt(slidingWindowSize, emitFrequencyInSeconds), 1).fieldsGrouping(parserId, new Fields(CONFIG_FILE.fieldHostNameParsed, CONFIG_FILE.fieldHostMsgParsed));
            //In case you want the combiner, uncomment the next line and make the necessary changes in the line above and in the uncommented line below.
            //builder.setBolt(preliminaryRankerId, new PreliminaryRankingBolt(TOP_N,emitFrequencyInSeconds,topic), 1).fieldsGrouping(counterId,new Fields(CONFIG_FILE.fieldRollingCountBoltOutput));
        builder.setBolt(topicRankerId, new TopicRankingBolt(TOP_N,emitFrequencyInSeconds,topic)).fieldsGrouping(counterId,new Fields(CONFIG_FILE.fieldRollingCountBoltOutputObject));

        
    }
    
}
