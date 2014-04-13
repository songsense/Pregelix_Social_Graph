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
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.io.DoubleWritable;

public class WeightedShortestPathsInputFormat extends 
TextVertexInputFormat<VLongWritable, DoubleWritable, FloatWritable, DoubleWritable>{
    @Override
    public VertexReader<VLongWritable, DoubleWritable, FloatWritable, DoubleWritable> createVertexReader(
            InputSplit split, TaskAttemptContext context) throws IOException {
        return new WeightedShortestPathsGraphReader(textInputFormat.createRecordReader(split, context));
    }
}

@SuppressWarnings("rawtypes")
class WeightedShortestPathsGraphReader extends
        TextVertexReader<VLongWritable, DoubleWritable, FloatWritable, DoubleWritable> {

    private final static String separator = " ";
    private Vertex vertex;
    private VLongWritable vertexId = new VLongWritable();
    private List<VLongWritable> pool = new ArrayList<VLongWritable>();
    private int used = 0;

    public WeightedShortestPathsGraphReader(RecordReader<LongWritable, Text> lineRecordReader) {
        super(lineRecordReader);
    }

    @Override
    public boolean nextVertex() throws IOException, InterruptedException {
        return getRecordReader().nextKeyValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Vertex<VLongWritable, DoubleWritable, FloatWritable, DoubleWritable> getCurrentVertex() throws IOException,
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
             * get neighbor num
             */
            int neighborNum = Integer.parseInt(fields[1]);
            // Debugging...
            System.out.print("Neighbor num: ");
            System.out.println(neighborNum);
            
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
                
                // Debugging...
                System.out.print("dest id: ");
                System.out.println(dest);
                System.out.print("weight: ");
                System.out.println(weight);
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