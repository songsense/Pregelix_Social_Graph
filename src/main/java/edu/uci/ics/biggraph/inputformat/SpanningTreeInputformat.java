package edu.uci.ics.biggraph.inputformat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.VertexReader;
import edu.uci.ics.pregelix.api.io.text.TextVertexInputFormat;
import edu.uci.ics.pregelix.api.io.text.TextVertexInputFormat.TextVertexReader;
import edu.uci.ics.pregelix.api.util.BspUtils;
import edu.uci.ics.biggraph.algo.SpanningTreeVertex;
import edu.uci.ics.biggraph.io.TwoVLongWritable;
import edu.uci.ics.biggraph.io.VLongWritable;

public class SpanningTreeInputformat extends
        TextVertexInputFormat<VLongWritable, VLongWritable, FloatWritable, TwoVLongWritable>{
    @Override
    public VertexReader<VLongWritable, VLongWritable, FloatWritable, TwoVLongWritable> createVertexReader(
            InputSplit split, TaskAttemptContext context) throws IOException {
        return new SpanningTreeGraphReader(textInputFormat.createRecordReader(split, context));
    }
}

@SuppressWarnings("rawtypes")
class SpanningTreeGraphReader extends
        TextVertexReader<VLongWritable, VLongWritable, FloatWritable, TwoVLongWritable> {

    private final static String separator = " ";
    private Vertex vertex;
    private VLongWritable vertexId = new VLongWritable();
    private List<VLongWritable> pool = new ArrayList<VLongWritable>();
    private int used = 0;
    // record the maximum num of out degree
    private int maxNumOutDegree = 0;

    public SpanningTreeGraphReader(RecordReader<LongWritable, Text> lineRecordReader) {
        super(lineRecordReader);
    }

    @Override
    public boolean nextVertex() throws IOException, InterruptedException {
        return getRecordReader().nextKeyValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Vertex<VLongWritable, VLongWritable, FloatWritable, TwoVLongWritable> getCurrentVertex() throws IOException,
            InterruptedException {
        if (vertex == null)
            vertex = (Vertex) BspUtils.createVertex(getContext().getConfiguration());

        vertex.getMsgList().clear();
        vertex.getEdges().clear();
        Text line = getRecordReader().getCurrentValue();
        String[] fields = line.toString().split(separator);
        /**
         * for data format, see:
         * https://docs.google.com/a/uci.edu/document/d/18jaKJT3OCVdKgXPB6JMClRKvsl2IWx8vJYfiTgFjxd0/edit
         */

        if (fields.length > 0) {
            /**
             * set the src vertex id
             */
            long src = Long.parseLong(fields[0]);
            vertexId.set(src);
            vertex.setVertexId(vertexId);
            long dest = -1L;

            /**
             * set the vertex value as initialization
             */
            VLongWritable vertexValue = new VLongWritable();
            vertexValue.set(-1L);
            vertex.setVertexValue(vertexValue);

            /**
             * get neighbor num
             */
            int neighborNum = Integer.parseInt(fields[1]);
            if (maxNumOutDegree < neighborNum) {
            	// update the maxNumOutDegree
            	maxNumOutDegree = neighborNum;
            	// set the vertex id with maximum out degree
            	getContext().getConfiguration().setLong(SpanningTreeVertex.ROOT_ID, src);
            }

            /**
             * set up edges & weights
             */
            for (int i = 0; i < neighborNum; ++i) {
                // set up edge
                dest = Long.parseLong(fields[2*i+2]);
                VLongWritable destId = allocate();
                destId.set(dest);

                // set up weight
                float weight = Float.parseFloat(fields[2*i+3]);
                FloatWritable weightWritable = new FloatWritable(weight);
                vertex.addEdge(destId, weightWritable);
            }
        }
        // vertex.sortEdges();
        return vertex;
    }

    private VLongWritable allocate() {
        if (used >= pool.size()) {
            VLongWritable value = new VLongWritable();
            pool.add(value);
            used++;
            return value;
        } else {
            VLongWritable value = pool.get(used);
            used++;
            return value;
        }
    }
}