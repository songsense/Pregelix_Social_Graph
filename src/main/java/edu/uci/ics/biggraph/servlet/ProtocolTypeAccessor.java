package edu.uci.ics.biggraph.servlet;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by liqiangw on 4/27/14.
 */
public class ProtocolTypeAccessor extends DataTypeAccessor {
    private static ProtocolTypeAccessor ourInstance = new ProtocolTypeAccessor();

    public static ProtocolTypeAccessor getInstance() {
        return ourInstance;
    }

    private static class Protocol {
        int id = 0;
        int load_graph = 0;
        int task1_status = 0;
        int task2_status = 0;
        int task3_status = 0;
        String graph_file_path = null;
        int number_of_iterations = 0;
        int source_id = Integer.MIN_VALUE;
        int target_id = Integer.MAX_VALUE;
        int number_of_results = 0;
    }

    private Protocol protocol = new Protocol();

    private ProtocolTypeAccessor() {
    }

    /**
     * Load the data entry from database.
     */
    @Override
    public void loadEntry() throws IOException {
        String queryURL = URLGenerator.query("Communication", "Protocol");
        queryURL = URLGenerator.cmdParser(queryURL);
        queryURL = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, queryURL);
        String payload = Commander.sendGet(queryURL);

        if (payload != null) {
            JsonReader jsonReader = Json.createReader(new StringReader(payload));
            JsonObject jsonObject = jsonReader.readObject();

            // read fields;
            protocol.id = jsonObject.getJsonNumber("id").intValue();
            protocol.load_graph = jsonObject.getJsonNumber("load_graph").intValue();
            protocol.task1_status = jsonObject.getJsonNumber("task1_status").intValue();
            protocol.task2_status = jsonObject.getJsonNumber("task2_status").intValue();
            protocol.task3_status = jsonObject.getJsonNumber("task3_status").intValue();
            protocol.graph_file_path = jsonObject.getString("graph_file_path");
            protocol.number_of_iterations = jsonObject.getJsonNumber("number_of_iterations").intValue();
            protocol.source_id = jsonObject.getJsonNumber("source_id").intValue();
            protocol.target_id = jsonObject.getJsonNumber("target_id").intValue();
            protocol.number_of_results = jsonObject.getJsonNumber("number_of_results").intValue();

            jsonReader.close();
        }
    }

    /**
     * Write back the entry to database.
     *
     * Note that this method must be called after called loadEntry().
     */
    @Override
    public void storeEntry() throws IOException {

    }
}
