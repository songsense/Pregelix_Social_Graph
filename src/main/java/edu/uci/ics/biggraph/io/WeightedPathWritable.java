package edu.uci.ics.biggraph.io;

import java.io.DataOutput;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.Text;

// first is the weight
// the rest are the path
public class WeightedPathWritable extends DoubleArrayListWritable {

	public WeightedPathWritable() {
        super();
        DoubleWritable dw = new DoubleWritable(Double.MAX_VALUE);
        this.add(dw);
	}

    public double getWeight() {
        DoubleWritable wr = (DoubleWritable) this.get(0);
        double weight = wr.get();
        return weight;
    }

    public String getPath() {
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < this.size(); ++i) {
            DoubleWritable wr = (DoubleWritable) this.get(i);
            long id = (long)(wr.get());             // note that the node id is long
            sb.append(id).append(" ");
        }
        return sb.toString();
    }

    public ArrayList<Double> getPathArrayList() {
        ArrayList<Double> path = new ArrayList<Double>();
        for (int i = 1; i < this.size(); ++i) {
            DoubleWritable wr = (DoubleWritable) this.get(i);
            path.add(wr.get());
        }
        return path;
    }

    public void setWeight(double weight) {
        DoubleWritable dw = new DoubleWritable(weight);
        this.set(0, dw);
    }

    public void setPath(ArrayList<Double> path, long id) {
        this.setPathAlone(path);
        double newID = (double) id;
        this.add((new DoubleWritable(newID)));
    }

    public void setPathAlone(ArrayList<Double> path) {
        double weight = getWeight();
        this.clear();
        this.add(new DoubleWritable(weight));
        for (int i = 0; i < path.size(); ++i) {
            this.add(new DoubleWritable(path.get(i)));
        }
    }

    @Override
    public String toString() {
        double weight = this.getWeight();
        String path = this.getPath();
        return Double.toString(weight) + "\t" + path;
    }
}
