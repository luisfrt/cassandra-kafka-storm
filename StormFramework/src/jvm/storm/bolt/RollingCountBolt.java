package storm.bolt;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

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
import java.lang.Math;
import storm.util.CONFIG_FILE;


public class RollingCountBolt extends AbstractSlidingWindowCounterBolt {

    private static final long serialVersionUID = 5537727428628598519L;
    private static final Logger LOG = Logger.getLogger(RollingCountBolt.class);

    public RollingCountBolt() {
        super();
    }

    public RollingCountBolt(int windowLengthInSeconds, int emitFrequencyInSeconds) {
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
        emit(counts, actualWindowLengthInSeconds);
    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(CONFIG_FILE.fieldRollingCountBoltOutputObject, CONFIG_FILE.fieldRollingCountBoltOutputObjectCount, CONFIG_FILE.fieldRollingCountBoltSlidingWindowSize));
    }

}
