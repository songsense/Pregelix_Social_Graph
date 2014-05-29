package edu.uci.ics.biggraph.servlet;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by liqiangw on 5/19/14.
 */
public class TaskFiveTypeAccessor extends DataTypeAccessor {

    private static TaskFiveTypeAccessor ourInstance = new TaskFiveTypeAccessor();

    // FIXME: Should we eliminate singleton pattern?
    public static TaskFiveTypeAccessor getInstance() {
//        return ourInstance;
        return new TaskFiveTypeAccessor();
    }

    /* Fields specification */
    /** id: string */
    private String id;
    /** login_user_id: int32 */
    private int login_user_id;
    /** source_node: int32 */
    private int source_node;
    /** label: string */
    private String label = "";
    /** target_nodes: [int32] */
    private LinkedList<Integer> target_nodes;

    private TaskFiveTypeAccessor() {
    }

    public void setVertex(String id, int login_user_id, int source_node,
                          String label, LinkedList<Integer> target_nodes) {
        this.id = id;
        this.login_user_id = login_user_id;
        this.source_node = source_node;
        this.label = label;
        this.target_nodes = target_nodes;
    }

    /**
     * Load the data entry from database.
     */
    @Override
    public void loadEntry() throws IOException {

    }

    /**
     * Write back the entry to database.
     *
     * Note that this method should be called after calling setVertex()
     * otherwise unexpected things will happen.
     */
    @Override
    public void storeEntry() throws IOException {
        removeEntry();

        String url = makeURL();
        Commander.sendGet(url);
    }

    private String assembleFields() {
        JsonObjectBuilder model = Json.createObjectBuilder()
                .add("id", id)
                .add("login_user_id", login_user_id)
                .add("source_node", source_node)
                .add("label", label);

        Iterator<Integer> it = target_nodes.iterator();
        JsonArrayBuilder t = Json.createArrayBuilder();
        while (it.hasNext()) {
            t.add(it.next());
        }
        model.add("target_nodes", t);

        return model.build().toString();
    }

    private String makeURL() {
        String cmds = assembleFields();
        String aql = URLGenerator.update("Graph", "DisplayGraph", cmds);
        aql = URLGenerator.cmdParser(aql);
        String url = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, aql);

        return url;
    }

    private void removeEntry() throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("use dataverse Graph;");
        sb.append("delete $t from dataset DisplayGraph where $t.id = \"" +
                id + "\";");

        String cmd = sb.toString();
        cmd = URLGenerator.cmdParser(cmd);
        cmd = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, cmd);
        Commander.sendGet(cmd);
    }

    public static void main(String[] args) {

    }
}
