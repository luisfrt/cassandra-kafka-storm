package storm.bolt;

import org.apache.log4j.Logger;

import storm.tools.Rankable;
import storm.tools.RankableObjectWithFields;
import backtype.storm.tuple.Tuple;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import storm.util.CONFIG_FILE;
/**
 * This bolt ranks incoming objects by their count.
 * 
 * It assumes the input tuples to adhere to the following format: (object, object_count, additionalField1,
 * additionalField2, ..., additionalFieldN).
 * 
 */
public final class PreliminaryRankingBolt extends AbstractRankerBolt {

    private static final long serialVersionUID = -1229432150246132104L;
    private static final Logger LOG = Logger.getLogger(PreliminaryRankingBolt.class);

    public PreliminaryRankingBolt() {
        super();
    }

    public PreliminaryRankingBolt(int topN, String boltName) {
        super(topN, boltName);
    }

    public PreliminaryRankingBolt(int topN, int emitFrequencyInSeconds, String boltName) {
        super(topN, emitFrequencyInSeconds, boltName);
    }

    @Override
    void updateRankingsWithTuple(Tuple tuple) {
        Rankable rankable = RankableObjectWithFields.from(tuple);
        super.getRankings().updateWith(rankable);
    }
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(CONFIG_FILE.fieldCombinerOutput));
    }

    @Override
    Logger getLogger() {
        return LOG;
    }
}
