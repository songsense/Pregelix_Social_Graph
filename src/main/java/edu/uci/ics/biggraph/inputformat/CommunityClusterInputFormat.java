package edu.uci.ics.biggraph.inputformat;

import edu.uci.ics.biggraph.io.VLongIntWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.VertexReader;
import edu.uci.ics.pregelix.api.io.text.TextVertexInputFormat;
import edu.uci.ics.pregelix.api.io.text.TextVertexInputFormat.TextVertexReader;
import edu.uci.ics.pregelix.api.util.BspUtils;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommunityClusterInputFormat extends
        TextVertexInputFormat<VLongWritable, VLongIntWritable, FloatWritable, VLongWritable>{
    @Override
    public VertexReader<VLongWritable, VLongIntWritable, FloatWritable, VLongWritable> createVertexReader(
            InputSplit split, TaskAttemptContext context) throws IOException {
        return new CommunityClusterGraphReader(textInputFormat.createRecordReader(split, context));
    }
}

@SuppressWarnings("rawtypes")
class CommunityClusterGraphReader extends
        TextVertexReader<VLongWritable, VLongIntWritable, FloatWritable, VLongWritable> {

    private final static String separator = " ";
    private Vertex vertex;
    private VLongWritable vertexId = new VLongWritable();
    private List<VLongWritable> pool = new ArrayList<VLongWritable>();
    private int used = 0;

    public CommunityClusterGraphReader(RecordReader<LongWritable, Text> lineRecordReader) {
        super(lineRecordReader);
    }

    @Override
    public boolean nextVertex() throws IOException, InterruptedException {
        return getRecordReader().nextKeyValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Vertex<VLongWritable, VLongIntWritable, FloatWritable, VLongWritable> getCurrentVertex() throws IOException,
            InterruptedException {
        if (vertex == null)
            vertex = (Vertex) BspUtils.createVertex(getContext().getConfiguration());

        vertex.getMsgList().clear();
        vertex.getEdges().clear();
        Text line = getRecordReader().getCurrentValue();
        String raw = line.toString();
        String[] fields = ADMParser.split(raw);
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
            VLongIntWritable vertexValue = new VLongIntWritable();
            vertexValue.set(src, 1);
            vertex.setVertexValue(vertexValue);

            /**
             * get neighbor num
             */
            int neighborNum = Integer.parseInt(fields[1]);

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
                weight = 1.0f / (weight + 0.001f);			// smaller the weight means more common tags
                FloatWritable weightWritable = new FloatWritable(weight);
                vertex.addEdge(destId, weightWritable);
            }
        }
        // vertex.sortEdges();
        return vertex;
    }

    private String[] getCurrentVertexFromAdm() throws IOException, InterruptedException {
        return null;
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