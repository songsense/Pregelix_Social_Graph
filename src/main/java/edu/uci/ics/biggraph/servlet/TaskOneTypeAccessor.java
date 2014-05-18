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
public class TaskOneTypeAccessor extends DataTypeAccessor {
    private static TaskOneTypeAccessor ourInstance = new TaskOneTypeAccessor();
    // FIXME: Should we eliminate singleton pattern?
    public static TaskOneTypeAccessor getInstance() {
        return new TaskOneTypeAccessor();
//        return ourInstance;
    }

    /* Fields specification */
    /** id: string (primary key) */
    String id;
    /** login user id (user_id: int32) */
    private int user_id;
    /** the destination id (target_user_id: int32) */
    private int target_user_id;
    /** the length of the path (length: int32) */
    private int length;
    /** the path from the src to dst (path: [int32]) */
    private LinkedList<Integer> path = null;

    // FIXME: old code to be deleted
    private int target_node;
    /** weight: double */
    private double weight;


    private TaskOneTypeAccessor() {
    }

    public void setVertex(String id, int user_id, int target_user_id,
                          int length, LinkedList<Integer> path) {
        System.out.println("[Task1:setVertex] id: " + id + ", user id: " + user_id +
                    "target: " + target_user_id + ", path length: " + length);
        this.id = id;
        this.user_id = user_id;
        this.target_user_id = target_user_id;
        this.length = length;
        this.path = path;
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
                .add("id", id)
                .add("user_id", user_id)
                .add("target_user_id", target_user_id)
                .add("length", length);

        Iterator<Integer> it = path.iterator();
        JsonArrayBuilder t = Json.createArrayBuilder();
        while (it.hasNext()) {
            t.add(it.next());
        }
//        t.addNull(); // requirement for ordered list
        model.add("path", t);

        return model.build().toString();
    }

    private String makeURL() {
        String cmds = assembleFields();
        String aql = URLGenerator.update("Tasks", "TaskOne", cmds);
        aql = URLGenerator.cmdParser(aql);
        String url = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, aql);

        return url;
    }

    private void removeEntry() throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("use dataverse Tasks;");
        sb.append("delete $t from dataset TaskOne where $t.id = " +
                id + ";");

        String cmd = sb.toString();
        cmd = URLGenerator.cmdParser(cmd);
        cmd = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, cmd);
        Commander.sendGet(cmd);
    }

    public static void main(String[] args) {
        TaskOneTypeAccessor one = getInstance();
        LinkedList<Integer> path = new LinkedList<Integer>();
        path.add(2);
        path.add(3);
//        one.setVertex(1, 2.4, path);
        System.out.println(one.assembleFields());
        System.out.println(one.makeURL());
    }
}
