package edu.uci.ics.biggraph.outputformat;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;

import edu.uci.ics.biggraph.io.DoubleWritable;
import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat.TextVertexWriter;
import edu.uci.ics.pregelix.example.io.VLongWritable;

public class InverseWriter  extends
TextVertexWriter<VLongWritable, DoubleWritable, FloatWritable> {
	public InverseWriter(RecordWriter<Text, Text> lineRecordWriter) {
		super(lineRecordWriter);
	}

	@Override
	public void writeVertex(Vertex<VLongWritable, DoubleWritable, FloatWritable, ?> vertex) throws IOException,
	    InterruptedException {
		DoubleWritable dw = vertex.getVertexValue();
		double d = 1.0 / dw.get();
		String double2Write = new String(Double.toString(d));
		getRecordWriter().write(new Text(vertex.getVertexId().toString()),
		        new Text(double2Write));
	}
}
