package edu.uci.ics.biggraph.servlet;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Graph Type Accessor - This class is shared by two graph types:
 * 1. GraphType (for OriginalGraph)
 * 2. BackBoneNodeType (for BackBoneGraph)
 *
 * Created by liqiangw on 4/27/14.
 */
public class GraphTypeAccessor extends DataTypeAccessor {
    private static GraphTypeAccessor origin = new GraphTypeAccessor(GType.ORIGIN);
    private static GraphTypeAccessor backbone = new GraphTypeAccessor(GType.BACKBONE);

    public static final GraphTypeAccessor ORIGIN = origin;
    public static final GraphTypeAccessor BACKBONE = backbone;

    private enum GType {ORIGIN, BACKBONE};
    private GType type;

    /* Fields specification */
    /** source_node : int32 (primary key) */
    private int source_node;
    /** target_node : {{int32}} */
    private LinkedList<Integer> target_nodes = null;
    /** weight: {{double}} */
    private LinkedList<Double> weight = null;

    private GraphTypeAccessor(GType type) {
        this.type = type;
    }

    /**
     * Load the data entry from database.
     */
    @Override
    public void loadEntry() throws IOException {
        source_node = Integer.MIN_VALUE;
        target_nodes = new LinkedList<Integer>();
        weight = new LinkedList<Double>();
        // TODO: implement if really need it.
    }

    /**
     * Write back the entry to database.
     */
    @Override
    public void storeEntry() throws IOException {
//        removeEntry();

        String cmds = assembleFields();
        String aql;
        if (type == GType.ORIGIN) {
            aql = URLGenerator.update("OriginalGraph", "Graph", cmds);
        } else {
            aql = URLGenerator.update("BackBoneGraph", "BackBoneNode", cmds);
        }
        aql = URLGenerator.cmdParser(aql);
        String url = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, aql);
//        Commander.sendGet(url);
        System.out.println(url);
    }

    /**
     * Remove this entry in the database before setting
     * new value(s).
     *
     * Note that for current version of AsterixDB, before updating
     * an existing entry in the database, this entry should be
     * delete in the first place.
     */
    private void removeEntry() throws IOException {
        StringBuilder sb = new StringBuilder();

        if (type == GType.ORIGIN) {
            sb.append("use dataverse OriginalGraph;");
            sb.append("delete $e from dataset Graph where $e.source_node = "
                    + source_node + ";");
        } else {
            sb.append("use dataverse BackBoneGraph;");
            sb.append("delete $e from dataset BackBoneNode where $e.source_node = "
                    + source_node + ";");
        }

        String cmd = sb.toString();
        cmd = URLGenerator.cmdParser(cmd);
        cmd = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, cmd);
        Commander.sendGet(cmd);
    }

    private String assembleFields() {
//        String[] cmds = new String[3];
//        StringBuilder sb = new StringBuilder();
//
//        cmds[0] = "\"source_node:\"" + getSourceNode();
//
//        sb.append("\"target_nodes:\"[");
//        int listLen = target_nodes.size();
//        Iterator<Integer> it = target_nodes.iterator();
//        while (it.hasNext()) {
//            sb.append(it.next() + ",");
//        }
//        sb.append("null]");
//        cmds[1] = sb.toString();
//
//        sb = new StringBuilder();
//        sb.append("\"weight:\"[");
//        int wLen = weight.size();
//        Iterator<Double> dt = weight.iterator();
//        while (dt.hasNext()) {
//            sb.append(dt.next() + ",");
//        }
//        sb.append("null]");
//        cmds[2] = sb.toString();
//
//        return cmds;
        JsonObjectBuilder model = Json.createObjectBuilder()
                .add("source_node", getSourceNode());

        Iterator<Integer> it = target_nodes.iterator();
        JsonArrayBuilder targets = Json.createArrayBuilder();
        while (it.hasNext()) {
            targets.add(it.next());
        }
        targets.addNull(); // requirement for ordered list
        model.add("target_nodes", targets);

        Iterator<Double> dt = weight.iterator();
        JsonArrayBuilder w = Json.createArrayBuilder();
        while (dt.hasNext()) {
            w.add(dt.next());
        }
        w.addNull();
        model.add("weight", w);

        return model.build().toString();
    }

    public int getSourceNode() {
        return source_node;
    }

    public void setSourceNode(int src) {
        source_node = src;
    }

    public LinkedList<Integer> getTargetNodes() {
        return target_nodes;
    }

    public void setTargetNodes(LinkedList<Integer> target) {
        target_nodes = target;
    }

    public LinkedList<Double> getWeight() {
        return weight;
    }

    public void setWeight(LinkedList<Double> weight) {
        this.weight = weight;
    }

    public static void main(String[] args) throws IOException {
        LinkedList<Integer> targets = new LinkedList<Integer>();
        LinkedList<Double> weights = new LinkedList<Double>();

        targets.add(1);
        targets.add(2);
        weights.add(0.4);
        weights.add(0.5);

        GraphTypeAccessor origin = GraphTypeAccessor.ORIGIN;
        origin.setSourceNode(0);
        origin.setTargetNodes(targets);
        origin.setWeight(weights);
        origin.storeEntry();
    }
}
