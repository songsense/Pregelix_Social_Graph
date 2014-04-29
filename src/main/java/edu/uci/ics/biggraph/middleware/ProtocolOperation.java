package edu.uci.ics.biggraph.middleware;

import edu.uci.ics.biggraph.servlet.Commander;
import edu.uci.ics.biggraph.servlet.RestAPI;
import edu.uci.ics.biggraph.servlet.URLGenerator;
import org.eclipse.jetty.util.ajax.JSON;

import java.io.IOException;
import java.util.HashMap;

/**
 * @deprecated Logic has been merged into GraphTypeAccessor.
 * Created by soushimei on 4/27/14.
 */
public class ProtocolOperation {
    public static String getInputGraphPath() throws IOException {
        return getValue(GRAPH_FILE_PATH);
    }

    public static String getSourceId() throws IOException {
        return getValue(SOURCE_ID);
    }

    public static String getIterations() throws IOException {
        return getValue(ITERATIONS);
    }

    private static String getValue(String fieldName) throws IOException {
        String fieldValue;
        String query = queryGenerator(fieldName);
        query = queryNormalizer(query);
        query = queryAPIGenerator(query);
        fieldValue = Commander.sendGet(query);

        HashMap<String,String[]> map = (HashMap<String,String[]>) JSON.parse(fieldValue);
        Object[] path = map.get("results");
        fieldValue = path[0].toString();
        if (fieldValue.endsWith("\n")) {
            fieldValue = fieldValue.substring(0, fieldValue.length()-1);
        }
        return fieldValue;
    }

    private static String queryNormalizer(String query) {
        return URLGenerator.cmdParser(query);
        // return URLEncoder.encode(query, "UTF-8");
    }

    private static String queryAPIGenerator(String query) {
        return URLGenerator.generate("localhost", 19002, RestAPI.QUERY, query);
    }

    private static String queryGenerator(String fieldName) {
        StringBuilder sb = new StringBuilder();

        sb.append("use dataverse " + DATAVERSE + ";")
                .append("for $l in dataset('" + DATASET + "') return $l.")
                .append(fieldName);

        return sb.toString();
    }

    private static final String DATAVERSE = "Communication";
    private static final String DATASET = "Protocol";
    private static final String GRAPH_FILE_PATH = "graph_file_path";
    private static final String SOURCE_ID = "source_id";
    private static final String ITERATIONS = "number_of_iterations";
}
