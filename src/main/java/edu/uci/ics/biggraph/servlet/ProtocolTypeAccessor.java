package edu.uci.ics.biggraph.servlet;

import javax.json.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Map;

/**
 * Created by liqiangw on 4/27/14.
 */
public class ProtocolTypeAccessor extends DataTypeAccessor {
    private static ProtocolTypeAccessor ourInstance = new ProtocolTypeAccessor();

    public static ProtocolTypeAccessor getInstance() {
        return ourInstance;
    }

    private static class Protocol {
        String id = "";
        String load_graph = "";
        String task1_status = "";
        String task2_status = "";
        String task3_status = "";
        String graph_file_path = "";
        String number_of_iterations = "";
        String source_id = "";
        String target_id = "";
        String number_of_results = "";
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

            payload = jsonObject.get("results").toString();
            jsonReader.close();
            jsonReader = Json.createReader(new StringReader(payload));
            JsonArray jsonArray = jsonReader.readArray();
            if (jsonArray.isEmpty()) {
                jsonReader.close();
                return;
            }
            payload = jsonArray.getString(0);

            jsonReader.close();
            jsonReader = Json.createReader(new StringReader(payload));
            Map map = (Map) jsonReader.readObject();
            String a = map.get("id").toString();

            // read fields;
            protocol.id = map.get("id").toString();
            protocol.load_graph = map.get("load_graph").toString();
            protocol.task1_status = map.get("task1_status").toString();
            protocol.task2_status = map.get("task2_status").toString();
            protocol.task3_status = map.get("task3_status").toString();
            protocol.graph_file_path = map.get("graph_file_path").toString();
            protocol.number_of_iterations = map.get("number_of_iterations").toString();
            protocol.source_id = map.get("source_id").toString();
            protocol.target_id = map.get("target_id").toString();
            protocol.number_of_results = map.get("number_of_results").toString();

            jsonReader.close();
        }
    }

    public String getID() {
        // always return 0
        return protocol.id;
    }

    public int getLoadGraphStatus() {
        return Integer.parseInt(protocol.load_graph);
    }

    public void setLoadGraphStatus(int status) {
        if (status < 0 || status > 2) {
            System.out.println("setLoadGraphStatus(" + status + "): Invalid argument");
        } else {
            protocol.load_graph = Integer.toString(status);
        }
    }

    public int getTaskStatus(int taskNum) {
        switch (taskNum) {
        case 1:
            return Integer.parseInt(protocol.task1_status);
        case 2:
            return Integer.parseInt(protocol.task2_status);
        case 3:
            return Integer.parseInt(protocol.task3_status);
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
                protocol.task1_status = Integer.toString(status);
                break;
            case 2:
                protocol.task2_status = Integer.toString(status);
                break;
            case 3:
                protocol.task3_status = Integer.toString(status);
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

    public String getMaxIterations() {
        return protocol.number_of_iterations;
    }

    public void setMaxIterations(int iterations) {
        if (iterations >= 0) {
            protocol.number_of_iterations = Integer.toString(iterations);
        }
    }

    public String getSourceID() {
        return protocol.source_id;
    }

    public void setSourceID(int id) {
        if (id >= 0) {
            protocol.source_id = Integer.toString(id);
        }
    }

    public String getTargetID() {
        return protocol.target_id;
    }

    public void setTargetID(int id) {
        if (id >= 0) {
            protocol.target_id = Integer.toString(id);
        }
    }

    public String getMaxResults() {
        return protocol.number_of_results;
    }

    public void setMaxResults(int results) {
        if (results >= 0) {
            protocol.number_of_results = Integer.toString(results);
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
                .add("load_graph", Integer.parseInt(protocol.load_graph))
                .add("task1_status", Integer.parseInt(protocol.task1_status))
                .add("task2_status", Integer.parseInt(protocol.task2_status))
                .add("task3_status", Integer.parseInt(protocol.task3_status))
                .add("graph_file_path", protocol.graph_file_path)
                .add("number_of_iterations", Integer.parseInt(protocol.number_of_iterations))
                .add("source_id", Integer.parseInt(protocol.source_id))
                .add("target_id", Integer.parseInt(protocol.target_id))
                .add("number_of_results", Integer.parseInt(protocol.number_of_results));

        return model.build().toString();
    }

    private String makeURL() {
        String cmds = assembleFields();
        String aql = URLGenerator.update("Communication", "Protocol", cmds);
        aql = URLGenerator.cmdParser(aql);
        String url = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, aql);

        return url;
    }

    public static void main(String[] args) {
        ProtocolTypeAccessor p = getInstance();
        System.out.println(p.assembleFields());
        System.out.println(p.makeURL());
    }
}
