package edu.uci.ics.biggraph.inputformat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.biggraph.io.VLongArrayListWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.VertexReader;
import edu.uci.ics.pregelix.api.io.text.TextVertexInputFormat;
import edu.uci.ics.pregelix.api.io.text.TextVertexInputFormat.TextVertexReader;
import edu.uci.ics.pregelix.api.util.BspUtils;

public class SocialSuggestionInputFormat extends 
TextVertexInputFormat<VLongWritable, VLongArrayListWritable, IntWritable, VLongArrayListWritable>{

    @Override
    public VertexReader<VLongWritable, VLongArrayListWritable, IntWritable, VLongArrayListWritable> createVertexReader(
            InputSplit split, TaskAttemptContext context) throws IOException {
        return new SocialSuggestionGraphReader(textInputFormat.createRecordReader(split, context));
    }
}

@SuppressWarnings("rawtypes")
class SocialSuggestionGraphReader extends
    TextVertexReader<VLongWritable, VLongArrayListWritable, IntWritable, VLongArrayListWritable> {
    private final static String separator = " ";
    
    private Vertex vertex;
    private VLongWritable vertexId = new VLongWritable();
    private List<VLongWritable> pool = new ArrayList<VLongWritable>();
    private int used = 0;
    
    public SocialSuggestionGraphReader(RecordReader<LongWritable, Text> arg0) {
        super(arg0);
    }

    @Override
    public boolean nextVertex() throws IOException, InterruptedException {
        return getRecordReader().nextKeyValue();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Vertex<VLongWritable, VLongArrayListWritable, IntWritable, VLongArrayListWritable> getCurrentVertex()
            throws IOException, InterruptedException {
        if (vertex == null)
            vertex = (Vertex) BspUtils.createVertex(getContext().getConfiguration());

        vertex.getMsgList().clear();
        vertex.getEdges().clear();
        Text line = getRecordReader().getCurrentValue();
        String[] fields = line.toString().split(separator);
        
        if (fields.length > 0) {
            // set source vertex ID
            long src = Long.parseLong(fields[0]);
            vertexId.set(src);
            vertex.setVertexId(vertexId);
            long dest = -1L;
            
            // set vertex value
            VLongArrayListWritable vertex = new VLongArrayListWritable();
            int numNeighbors = Integer.parseInt(fields[1]);
            for (int i = 0; i < numNeighbors; i++) {
                dest = Long.parseLong(fields[2*i+2]);
                VLongWritable destId = allocate();
                destId.set(dest);
            }
            
            // TODO: in the future, interest tags will be included
        }
        
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
