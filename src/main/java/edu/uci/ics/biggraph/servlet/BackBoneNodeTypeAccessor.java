package edu.uci.ics.biggraph.servlet;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.LinkedList;

/**
 * @deprecated Logic has been merged into GraphTypeAccessor.
 * Created by liqiangw on 4/27/14.
 */
public class BackBoneNodeTypeAccessor extends DataTypeAccessor {
    private static BackBoneNodeTypeAccessor ourInstance = new BackBoneNodeTypeAccessor();

    public static BackBoneNodeTypeAccessor getInstance() {
        return ourInstance;
    }

    /* Fields specification */
    /** source_node : int32 (primary key) */
    private int source_node;
    /** target_node : {{int32}} */
    private LinkedList<Integer> target_nodes = null;
    /** weight: {{double}} */
    private LinkedList<Double> weight = null;

    private BackBoneNodeTypeAccessor() {
    }

    /**
     * Load the data entry from database.
     */
    @Override
    public void loadEntry() {
        // TODO: implement if really need it.
    }

    /**
     * Write back the entry to database.
     */
    @Override
    public void storeEntry() {

    }

    public static void main(String[] args) {
        JsonObject model = Json.createObjectBuilder()
            .add("node_id", 0)
            .add("friends", Json.createArrayBuilder()
                    .add(2)
                    .add(3)
                    .addNull())
            .build();

        System.out.println(model.toString());
    }
}
