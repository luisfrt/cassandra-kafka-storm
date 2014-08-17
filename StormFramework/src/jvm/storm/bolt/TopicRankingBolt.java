package storm.bolt;

import org.apache.log4j.Logger;

import storm.tools.Rankable;
import storm.tools.RankableObjectWithFields;
import storm.tools.Rankings;
import backtype.storm.tuple.Tuple;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import storm.util.CONFIG_FILE;

public final class TopicRankingBolt extends AbstractRankerBolt {
    
    private static final long serialVersionUID = -4122545591555302143L;
    private static final Logger LOG = Logger.getLogger(TopicRankingBolt.class);
    
    public TopicRankingBolt() {
        super();
    }
    
    public TopicRankingBolt(int topN, String boltName) {
        super(topN, boltName);
    }
    
    public TopicRankingBolt(int topN, int emitFrequencyInSeconds, String boltName) {
        super(topN, emitFrequencyInSeconds, boltName);
    }
    
    @Override
    void updateRankingsWithTuple(Tuple tuple) {
        Rankable rankable = RankableObjectWithFields.from(tuple);
        super.getRankings().updateWith(rankable);
        //In case the combiner (PreliminaryRankingBolt) is set up between the RollingCountBolt and the TopicRankingBolt, comment the two lines above and uncomment the two below.
            //Rankings rankingsToBeMerged = (Rankings) tuple.getValue(0);
            //super.getRankings().updateWith(rankingsToBeMerged);
        writeOutputToFile();
        
    }
    
    @Override
    Logger getLogger() {
        return LOG;
    }
    
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(CONFIG_FILE.fieldTopicRankingBoltOutput));
    }
    
    void writeOutputToFile(){
        PrintWriter writer = null;
        
        try {
            File file = new File(CONFIG_FILE.stormOutputPath + super.boltName + ".txt");
            file.getParentFile().mkdirs();
            writer = new PrintWriter(file);
            writer.println(super.getRankings() + "\n");
            
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
}
