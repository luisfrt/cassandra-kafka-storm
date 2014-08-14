package storm.bolt;

import org.apache.log4j.Logger;

import storm.tools.*;
import backtype.storm.tuple.Tuple;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.Math.*;
import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import storm.tools.NthLastModifiedTimeTracker;
import storm.tools.SlidingWindowCounter;
import storm.util.TupleHelpers;
import backtype.storm.Config;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import storm.util.CONFIG_FILE;

public final class GlobalRankingsBolt extends AbstractSlidingWindowCounterBolt {
    
    private static final long serialVersionUID = -7654321805932302333L;
    private static final Logger LOG = Logger.getLogger(GlobalRankingsBolt.class);
    
    public GlobalRankingsBolt() {
        super();
    }
    
    public GlobalRankingsBolt(int windowLengthInSeconds, int emitFrequencyInSeconds) {
        super(windowLengthInSeconds, emitFrequencyInSeconds);
    }
    
    
    @Override
    protected void emitCurrentWindowCounts() {
        Map<Object, Long> counts = counter.getCountsThenAdvanceWindow();
        int actualWindowLengthInSeconds = lastModifiedTracker.secondsSinceOldestModification();
        lastModifiedTracker.markAsModified();
        if (actualWindowLengthInSeconds != windowLengthInSeconds) {
            LOG.warn(String.format(WINDOW_LENGTH_WARNING_TEMPLATE, actualWindowLengthInSeconds, windowLengthInSeconds));
        }
        
        writeOutputToFile();
        getLogger().info("\n\n ==================================\n "+ "Global Rankings: " + counts + " \n ==================================\n\n");
        //emit(counts, actualWindowLengthInSeconds);
    }
    
    @Override
    protected void countObjAndAck(Tuple tuple) {
        Rankings rankingsToBeMerged = (Rankings) tuple.getValue(0);
        //Replace the current value in the rank by the percentage of its value when compared to the total sum of all the values in the rank.
        int rankSize = rankingsToBeMerged.size();
        List<Rankable> listOfRanks =  rankingsToBeMerged.getRankings();
        long sum = 0;
        
        for (Rankable individualRank : listOfRanks ){
            
            sum += individualRank.getCount();
        }
        Rankings newRanking = new Rankings();
        
        for (int i =0; i<rankSize; i++){
            long percentage = 0;
            if(sum != 0) percentage = (long) Math.round(100*listOfRanks.get(i).getCount()/sum);
            counter.increaseCount(listOfRanks.get(i).getObject(),percentage);
            
        }
        collector.ack(tuple);
    }
    
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(CONFIG_FILE.fieldGlobalRankingBoltOutputObject, CONFIG_FILE.fieldGlobalRankingCountBoltOutputObjectCount, CONFIG_FILE.fieldGlobalRankingCountBoltSlidingWindowSize));
    }
    
    private void writeOutputToFile(){
        PrintWriter writer = null;
        
        try {
            File file = new File(CONFIG_FILE.stormOutputPath + CONFIG_FILE.fieldGlobalRankingBoltOutputObject+".txt");
            file.getParentFile().mkdirs();
            writer = new PrintWriter(file);
            //Sort the map before writing to a file
            ValueComparator bvc =  new ValueComparator(counter.getCounts());
            TreeMap<Object,Long> sorted_map = new TreeMap<Object,Long>(bvc);
            sorted_map.putAll(counter.getCounts());
            writer.println(sorted_map + "\n");
            
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if ( writer != null )
            {
                writer.close();
            }
        }
        
        
    }
    //Class used to sort the MAP.
    class ValueComparator implements Comparator<Object> {
        
        Map<Object, Long> base;
        public ValueComparator(Map<Object, Long> base) {
            this.base = base;
        }
        
        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(Object a, Object b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }
}
