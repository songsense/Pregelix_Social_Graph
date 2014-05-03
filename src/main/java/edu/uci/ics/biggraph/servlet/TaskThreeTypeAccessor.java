package edu.uci.ics.biggraph.servlet;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by liqiangw on 4/28/14.
 */
public class TaskThreeTypeAccessor extends DataTypeAccessor {
    private static TaskThreeTypeAccessor ourInstance = new TaskThreeTypeAccessor();

    // FIXME: Should we eliminate singleton pattern?
    public static TaskThreeTypeAccessor getInstance() {
//        return ourInstance;
        return new TaskThreeTypeAccessor();
    }

    /* Fields specification */
    /** node_id: int32 (primary key) */
    private int node_id;
    /** suggested_friends: [int32] */
    private LinkedList<Integer> suggested_friends = null;

    private TaskThreeTypeAccessor() {
    }

    public void setVertex(int node_id, LinkedList<Integer> suggested_friends) {
        synchronized (this) {
            assert node_id >= 0;

            this.node_id = node_id;
            this.suggested_friends = suggested_friends;
        }
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
            removeEntry();

            String url = makeURL();
            Commander.sendGet(url);
        }
    }

    private String assembleFields() {
        JsonObjectBuilder model = Json.createObjectBuilder()
                .add("node_id", node_id);

        Iterator<Integer> it = suggested_friends.iterator();
        JsonArrayBuilder t = Json.createArrayBuilder();
        while (it.hasNext()) {
            t.add(it.next());
        }
//        t.addNull(); // requirement for ordered list
        model.add("suggested_friends", t);

        return model.build().toString();
    }

    private String makeURL() {
        String cmds = assembleFields();
        String aql = URLGenerator.update("Tasks", "TaskThree", cmds);
        aql = URLGenerator.cmdParser(aql);
        String url = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, aql);

        return url;
    }

    private void removeEntry() throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("use dataverse Tasks;");
        sb.append("delete $t from dataset TaskThree where $t.node_id = " +
                node_id + ";");

        String cmd = sb.toString();
        cmd = URLGenerator.cmdParser(cmd);
        cmd = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, cmd);
        Commander.sendGet(cmd);
    }

    public static void main(String[] args) {
        TaskThreeTypeAccessor one = getInstance();
        LinkedList<Integer> path = new LinkedList<Integer>();
        path.add(2);
        path.add(3);

        one.setVertex(1, path);
        System.out.println(one.assembleFields());
        System.out.println(one.makeURL());
    }
}
