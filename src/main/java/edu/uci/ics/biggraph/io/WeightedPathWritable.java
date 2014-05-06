package edu.uci.ics.biggraph.io;

import java.util.ArrayList;

// first is the weight
// the rest are the path
@SuppressWarnings("serial")
public class WeightedPathWritable extends DoubleArrayListWritable {

	@SuppressWarnings("unchecked")
	public WeightedPathWritable() {
        super();
        DoubleWritable dw = new DoubleWritable(Double.MAX_VALUE);
        this.add(new DoubleWritable(dw.get()));
    	// here we assume the length of the array list is only 100
        // @see setPathAlone()
        for (int i = 0; i < 100; ++i) {
        	this.add(new DoubleWritable(Double.MAX_VALUE));
        }
	}

    @SuppressWarnings("unchecked")
	public WeightedPathWritable(WeightedPathWritable weightedPathWritable) {
        super();
        for (int i = 0; i < weightedPathWritable.size(); ++i) {
            DoubleWritable doubleWritable = (DoubleWritable) weightedPathWritable.get(i);
            this.add(new DoubleWritable(doubleWritable.get()));
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    public WeightedPathWritable clone() {
        WeightedPathWritable weightedPathWritable = new WeightedPathWritable();
        for (int i = 0; i < this.size(); ++i) {
            DoubleWritable doubleWritable = (DoubleWritable) this.get(i);
            weightedPathWritable.add(new DoubleWritable(doubleWritable.get()));
        }
        return weightedPathWritable;
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
            double idDouble = wr.get();
            if (idDouble == Double.MAX_VALUE) {
            	break;
            }
            long id = (long)(wr.get());             // note that the node id is long
            sb.append(id).append(" ");
        }
        return sb.toString();
    }

    public ArrayList<Double> getPathArrayList() {
        ArrayList<Double> path = new ArrayList<Double>();
        for (int i = 1; i < this.size(); ++i) {
            DoubleWritable wr = (DoubleWritable) this.get(i);
            double idDouble = wr.get();
            if (idDouble == Double.MAX_VALUE) {
            	break;
            }
            path.add(wr.get());
        }
        return path;
    }

    @SuppressWarnings("unchecked")
	public void setWeight(double weight) {
        DoubleWritable dw = new DoubleWritable(weight);
        this.set(0, dw);
    }

    @SuppressWarnings("unchecked")
	public void setPath(ArrayList<Double> path, long id) {
        this.setPathAlone(path);
        double newID = (double) id;
        this.set(path.size() + 1, (new DoubleWritable(newID)));
    }

    @SuppressWarnings("unchecked")
	public void setPathAlone(ArrayList<Double> path) {
        double weight = getWeight();
        this.set(0, new DoubleWritable(weight));
        for (int i = 0; i < path.size(); ++i) {
        	this.set(i + 1, new DoubleWritable(path.get(i)));
        }
        for (int i = path.size() + 1; i < 101; ++i) {
        	// here we assume the length of the array list is only 100
        	// @see constructor for details
        	this.set(i, new DoubleWritable(Double.MAX_VALUE));
        }
    }

    @Override
    public String toString() {
        double weight = this.getWeight();
        String path = this.getPath();
        return Double.toString(weight) + "\t" + path;
    }
}
