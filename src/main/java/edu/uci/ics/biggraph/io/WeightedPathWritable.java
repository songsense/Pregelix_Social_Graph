package edu.uci.ics.biggraph.io;

import java.io.DataOutput;
import java.io.DataInput;
import java.io.IOException;

import edu.uci.ics.pregelix.api.io.WritableSizable;
import org.apache.hadoop.io.Text;


public class WeightedPathWritable implements WritableSizable {
	// Weight as a double
	private DoubleWritable weight;
	// Path as a String
	private Text path;

	public WeightedPathWritable() {
		this.weight = new DoubleWritable();
		this.path = new Text();
	}

    @Override
    public void write(DataOutput dataOutput) throws IOException{
        weight.write(dataOutput);
        path.write(dataOutput);
    }
    @Override
    public void readFields(DataInput dataInput) throws IOException {
        weight.readFields(dataInput);
        path.readFields(dataInput);
    }

    public double getWeight() { return weight.get(); }

    public String getPath() {
        return path.toString();
    }

    public Text getPathWritable() {
        return path;
    }

    public void setWeight(double weight) {
        this.weight.set(weight);
    }

    public void setPath(Text path, long id) {
        String pathStr = path.toString() + " " + Long.toString(id);
        this.path.set(pathStr);
    }

    public void setPathAlone(Text path) {
        this.path.set(path.toString());
    }

    public int sizeInBytes() {
        return weight.sizeInBytes() + path.getLength() * 8;
    }

    @Override
    public String toString() {
        return weight.toString() + "\t" + path.toString();
    }
}
