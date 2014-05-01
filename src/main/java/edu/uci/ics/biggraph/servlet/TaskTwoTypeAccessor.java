package edu.uci.ics.biggraph.servlet;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.IOException;

/**
 * Created by liqiangw on 4/28/14.
 */
public class TaskTwoTypeAccessor extends DataTypeAccessor {
    private static TaskTwoTypeAccessor ourInstance = new TaskTwoTypeAccessor();

    // FIXME: Should we eliminate singleton pattern?
    public static TaskTwoTypeAccessor getInstance() {
//        return ourInstance;
        return new TaskTwoTypeAccessor();
    }

    /* Fields specification */
    /** node_id: int32 (primary key) */
    private int node_id;
    /** community_id: int32 */
    private int community_id;

    private TaskTwoTypeAccessor() {
    }

    synchronized public void setVertex(int node_id, int community_id) {
        System.out.println("[TaskTwo: setVertex]:" + node_id + " " + community_id);
        assert node_id >= 0;

        this.node_id = node_id;
        this.community_id = community_id;
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
        synchronized (this) {
            System.out.println("[Task2 storeEntry():" + node_id);
            removeEntry();

            String url = makeURL();
            Commander.sendGet(url);
        }
    }

    private String assembleFields() {
        JsonObjectBuilder model = Json.createObjectBuilder()
                .add("node_id", node_id)
                .add("community_id", community_id);

        return model.build().toString();
    }

    private String makeURL() {
        String cmds = assembleFields();
        String aql = URLGenerator.update("Tasks", "TaskTwo", cmds);
        aql = URLGenerator.cmdParser(aql);
        String url = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, aql);

        return url;
    }

    private void removeEntry() throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("use dataverse Tasks;");
        sb.append("delete $t from dataset TaskTwo where $t.node_id = " +
                node_id + ";");

        String cmd = sb.toString();
        cmd = URLGenerator.cmdParser(cmd);
        cmd = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, cmd);
        Commander.sendGet(cmd);
    }

    public static void main(String[] args) {
        TaskTwoTypeAccessor one = getInstance();

        one.setVertex(1, 2);
        System.out.println(one.assembleFields());
        System.out.println(one.makeURL());
    }
}
