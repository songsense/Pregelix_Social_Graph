package edu.uci.ics.biggraph.outputformat;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;

import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.VLongIntWritable;
import edu.uci.ics.biggraph.servlet.TaskTwoTypeAccessor;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat;
import edu.uci.ics.pregelix.example.io.VLongWritable;

/**
 * Created by soushimei on 4/19/14.
 */
public class CommunityClusterWriter extends
        TextVertexOutputFormat.TextVertexWriter<VLongWritable, VLongIntWritable, FloatWritable> {
    public CommunityClusterWriter(RecordWriter<Text, Text> lineRecordWriter) {
        super(lineRecordWriter);
    }

    @Override
    public void writeVertex(Vertex<VLongWritable, VLongIntWritable, FloatWritable, ?> vertex) throws IOException,
            InterruptedException {
        getRecordWriter().write(new Text(vertex.getVertexId().toString()),
                new Text(vertex.getVertexValue().toString()));
        /*  no database access
        TaskTwoTypeAccessor t = TaskTwoTypeAccessor.getInstance();
        t.setVertex((int) vertex.getVertexId().get(), (int) vertex.getVertexValue().getVertexId());
        t.storeEntry();
        */
    }
}