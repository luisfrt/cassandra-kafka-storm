package storm.bolt;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import storm.tools.NthLastModifiedTimeTracker;
import storm.tools.SlidingWindowCounter;
import storm.util.TupleHelpers;
import backtype.storm.Config;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.task.ShellBolt;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.base.BaseBasicBolt;
import storm.util.CONFIG_FILE;
/*
 * Parses messages from Kafka in the format: "(STRING),(LONG)"
 * e.g. : Message from server google.com -> google.com,7
 * e.g.2: Message from somewhere -> UniversidadeDeBrasilia,9
 *
*/
public class KafkaMessageParser extends BaseBasicBolt {
    
    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        try{
        String message = tuple.getString(0);
        List<String> hostnameAndValue = Arrays.asList(message.split(","));
        String hostname = hostnameAndValue.get(0);
        Long value = Long.parseLong(hostnameAndValue.get(1));
        //System.out.println("\n\n==================================\n NEW MESSAGE FROM KAFKA! \n Host = " + hostname + " Value = " + value + " \n==================================\n\n");
        collector.emit(new Values(hostname, value));
        }
        catch (Exception e){
            System.err.println("\n\n==================================\n WEIRD MESSAGE RECEIVED FROM KAFKA! IGNORING IT... \n==================================\n\n");
        }
    }
    
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(CONFIG_FILE.fieldHostNameParsed, CONFIG_FILE.fieldHostMsgParsed));
    }
}
