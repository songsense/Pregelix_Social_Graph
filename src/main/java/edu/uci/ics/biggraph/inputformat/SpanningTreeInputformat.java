package edu.uci.ics.biggraph.inputformat;

import edu.uci.ics.biggraph.io.HelloCntParentIdWritable;
import edu.uci.ics.biggraph.io.IntWritable;
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

public class SpanningTreeInputformat extends
        TextVertexInputFormat<VLongWritable, IntWritable, FloatWritable, HelloCntParentIdWritable>{
    @Override
    public VertexReader<VLongWritable, IntWritable, FloatWritable, HelloCntParentIdWritable> createVertexReader(
            InputSplit split, TaskAttemptContext context) throws IOException {
        return new SpanningTreeGraphReader(textInputFormat.createRecordReader(split, context));
    }
}

@SuppressWarnings("rawtypes")
class SpanningTreeGraphReader extends
        TextVertexReader<VLongWritable, IntWritable, FloatWritable, HelloCntParentIdWritable> {

    private final static String separator = " ";
    private Vertex vertex;
    private VLongWritable vertexId = new VLongWritable();
    private IntWritable vertexValue = new IntWritable();
    private List<VLongWritable> pool = new ArrayList<VLongWritable>();
    private int used = 0;
    // record the maximum num of out degree
//    private static int maxNumOutDegree = 0;
    private static boolean isFirst = true;

    public SpanningTreeGraphReader(RecordReader<LongWritable, Text> lineRecordReader) {
        super(lineRecordReader);
    }

    @Override
    public boolean nextVertex() throws IOException, InterruptedException {
        return getRecordReader().nextKeyValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Vertex<VLongWritable, IntWritable, FloatWritable, HelloCntParentIdWritable> getCurrentVertex() throws IOException,
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
            if (isFirst) {
            	vertexValue.set(0);
            	vertex.setVertexValue(vertexValue);
            	isFirst = false;
            } else {
            	vertexValue.set(Integer.MAX_VALUE);
            	vertex.setVertexValue(vertexValue);
            }

            /**
             * get neighbor num
             */
            int neighborNum = Integer.parseInt(fields[1]);
//            if (maxNumOutDegree < neighborNum) {
//            	// update the maxNumOutDegree
//            	maxNumOutDegree = neighborNum;
//            	// set the vertex id with maximum out degree
//            	getContext().getConfiguration().setLong(SpanningTreeVertex.ROOT_ID, src);
//            	System.out.print(src);
//            	System.out.print(" ");
//            	System.out.print(getContext().getConfiguration().getLong(SpanningTreeVertex.ROOT_ID, 0));
//            	System.out.print(" ");
//            }

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