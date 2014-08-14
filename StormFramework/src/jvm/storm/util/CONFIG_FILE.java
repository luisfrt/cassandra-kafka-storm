package storm.util;

public class CONFIG_FILE{
    //Topology name
    final public static String topologyName = "Monitor for Geo Distributed Servers";
    //Maximum number of servers that will be monitored.
    final public static int TOP_N = 30;
    //How many seconds in the past should the system keep information about the servers.
    final public static int slidingWindowSize = 60;
    //The emit frequency from a Bolt to another.
    final public static int emitFrequencyInSeconds = 1;
    
    // -------- Kafka Setup --------
    
    //Kafka Broker Ip Address and Port
    final public static String kafkaBrokerIp = "194.36.10.160:2181";
    
    //Kafka Config parameters
    final public static String brokers = "/brokers";
    final public static String zookeperRootPath = "/kafkastorm";
    final public static String consumerID = "discovery";
    
    //Kafka topics that are beeing watched. If you want more topics, just add here.
    public enum Topics {
        readLatency,
        writeLatency,
        pendingTasks,
        cpuLoad,
        memUsage;
    }

    // ----- End of Kafka setup ----
    
    //Bolt names
    final public static String kafkaSpoutName = "KafkaSpout";
    final public static String kafkaMessageParserBoltName = "KafkaParser";
    final public static String rollingCountBoltName = "Counter";
    final public static String combinerBoltName = "PreliminaryRanker";
    final public static String topicRankerBoltName = "TopicRanker";
    final public static String globalRankingsBoltName = "GlobalRanker";
    
    //STORM OUTPUT PATH
    final public static String stormOutputPath = "./STORM_OUTPUT/";
    
    //Fields names to connect bolts
    
        //Kafka Parser Output Fields
    final public static String fieldHostNameParsed = "word";
    final public static String fieldHostMsgParsed = "count";
        //Rolling Count Bolt Output Fields
    final public static String fieldRollingCountBoltOutputObject = "obj";
    final public static String fieldRollingCountBoltOutputObjectCount = "count";
    final public static String fieldRollingCountBoltSlidingWindowSize = "actualWindowLengthInSeconds";
        //Combiner Bolt (PreliminaryRankingBolt) Output Fields
    final public static String fieldCombinerOutput = "preliminaryRankings";
        //Topic Ranking Bolt
    final public static String fieldTopicRankingBoltOutput = "topicRankings";
        //Global Ranking Bolt
    final public static String fieldGlobalRankingBoltOutputObject = "GlobalRankings";
    final public static String fieldGlobalRankingCountBoltOutputObjectCount = "count";
    final public static String fieldGlobalRankingCountBoltSlidingWindowSize = "actualWindowLengthInSeconds";
    
    
        //Abstract Sliding Window Counter Bolt
    final public static String fieldSlidingWindowCounterBoltOutputObject = "obj";
    final public static String fieldSlidingWindowCounterBoltOutputObjectCount = "count";
    final public static String fieldSlidingWindowCounterBoltSlidingWindowSize = "actualWindowLengthInSeconds";
        //Abstract Ranker Bolt
    final public static String fieldAbstractRankerBoltOutput = "rankings";
}