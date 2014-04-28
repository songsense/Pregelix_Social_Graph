package edu.uci.ics.biggraph.servlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by liqiangw on 4/27/14.
 */
public class GraphTypeAccessor extends DataTypeAccesor {
    private static GraphTypeAccessor ourInstance = new GraphTypeAccessor();

    public static GraphTypeAccessor getInstance() {
        return ourInstance;
    }

    /* Fields specification */
    /** source_node : int32 (primary key) */
    private int source_node;
    /** target_node : {{int32}} */
    private LinkedList<Integer> target_nodes = null;
    /** weight: {{double}} */
    private LinkedList<Double> weight = null;

    private GraphTypeAccessor() {
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
        removeEntry();

        String[] cmds = assembleFields();
        String aql = URLGenerator.update("OriginalGraph", "Graph", cmds);
        aql = URLGenerator.cmdParser(aql);
        String url = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, aql);
        Commander.sendGet(url);
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

        sb.append("use dataverse OriginalGraph;");
        sb.append("delete $e from dataset Graph where $e.source_node = "
                    + source_node + ";");

        String cmd = sb.toString();
        cmd = URLGenerator.cmdParser(cmd);
        cmd = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, cmd);
        Commander.sendGet(cmd);
    }

    private String[] assembleFields() {
        String[] cmds = new String[3];
        StringBuilder sb = new StringBuilder();

        cmds[0] = "\"source_node:\"" + getSourceNode();

        sb.append("\"target_nodes:\"{{");
        int listLen = target_nodes.size();
        Iterator<Integer> it = target_nodes.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (--listLen > 0) {
                sb.append(",");
            }
        }
        sb.append("}}");
        cmds[1] = sb.toString();

        sb = new StringBuilder();
        sb.append("\"weight:\"{{");
        int wLen = weight.size();
        Iterator<Double> dt = weight.iterator();
        while (dt.hasNext()) {
            sb.append(dt.next());
            if (--wLen > 0) {
                sb.append(",");
            }
        }
        sb.append("}}");
        cmds[2] = sb.toString();

        return cmds;
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
}
