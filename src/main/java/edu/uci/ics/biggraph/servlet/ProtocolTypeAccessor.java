package edu.uci.ics.biggraph.servlet;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
        String graph_file_path = "";
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
        queryURL = URLGenerator.generate("localhost", 19002, RestAPI.QUERY, queryURL);
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

    public int getID() {
        // always return 0
        return protocol.id;
    }

    public int getLoadGraphStatus() {
        return protocol.load_graph;
    }

    public void setLoadGraphStatus(int status) {
        if (status < 0 || status > 2) {
            System.out.println("setLoadGraphStatus(" + status + "): Invalid argument");
        } else {
            protocol.load_graph = status;
        }
    }

    public int getTaskStatus(int taskNum) {
        switch (taskNum) {
        case 1:
            return protocol.task1_status;
        case 2:
            return protocol.task2_status;
        case 3:
            return protocol.task3_status;
        default:
            return -1;
        }
    }

    public void setTaskStatus(int taskNum, int status) {
        if (status < 0 || status > 2 || taskNum < 0 || taskNum > 2) {
            System.out.println("setTaskStatus(" + taskNum + ","
                    + status + "): Invalid argument");
        } else {
            switch (taskNum) {
            case 1:
                protocol.task1_status = status;
                break;
            case 2:
                protocol.task2_status = status;
                break;
            case 3:
                protocol.task3_status = status;
                break;
            default:
                break;
            }
        }
    }

    public String getGraphFilePath() {
        return protocol.graph_file_path;
    }

    public void setGraphFilePath(String path) {
        protocol.graph_file_path = path;
    }

    public int getMaxIterations() {
        return protocol.number_of_iterations;
    }

    public void setMaxIterations(int iterations) {
        if (iterations >= 0) {
            protocol.number_of_iterations = iterations;
        }
    }

    public int getSourceID() {
        return protocol.source_id;
    }

    public void setSourceID(int id) {
        if (id >= 0) {
            protocol.source_id = id;
        }
    }

    public int getTargetID() {
        return protocol.target_id;
    }

    public void setTargetID(int id) {
        if (id >= 0) {
            protocol.target_id = id;
        }
    }

    public int getMaxResults() {
        return protocol.number_of_results;
    }

    public void setMaxResults(int results) {
        if (results >= 0) {
            protocol.number_of_results = results;
        }
    }

    /**
     * Write back the entry to database.
     *
     * Note that this method must be called after called loadEntry().
     */
    @Override
    public void storeEntry() throws IOException {
        removeEntry();

        String url = makeURL();
        Commander.sendGet(url);
    }

    private void removeEntry() throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("use dataverse Communication;");
        sb.append("delete $c from dataset Protocol where $c.id = 0;");

        String cmd = sb.toString();
        cmd = URLGenerator.cmdParser(cmd);
        cmd = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, cmd);
        Commander.sendGet(cmd);
    }

    private String assembleFields() {
        JsonObjectBuilder model = Json.createObjectBuilder()
                .add("id", 0)
                .add("load_graph", protocol.load_graph)
                .add("task1_status", protocol.task1_status)
                .add("task2_status", protocol.task2_status)
                .add("task3_status", protocol.task3_status)
                .add("graph_file_path", protocol.graph_file_path)
                .add("number_of_iterations", protocol.number_of_iterations)
                .add("source_id", protocol.source_id)
                .add("target_id", protocol.target_id)
                .add("number_of_results", protocol.number_of_results);

        return model.build().toString();
    }

    private String makeURL() {
        String cmds = assembleFields();
        String aql = URLGenerator.update("Communication", "Protocol", cmds);
        aql = URLGenerator.cmdParser(aql);
        String url = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, aql);

        return url;
    }

    public static void main(String[] args) throws IOException {
        ProtocolTypeAccessor p = getInstance();
        System.out.println(p.assembleFields());
        System.out.println(p.makeURL());

        System.out.println("-----Json Reader Test-----");
        p.loadEntry();
    }

}
