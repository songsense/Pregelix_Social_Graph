package edu.uci.ics.biggraph.servlet;

import java.io.IOException;

/**
 * Created by liqiangw on 4/28/14.
 */
public class DatabaseInitializer {
    private static final String INIT_GRAPH_STATEMENT =
            "drop dataverse OriginalGraph if exists;" +
            "create dataverse OriginalGraph;" +
            "use dataverse OriginalGraph;" +
            "create type GraphType as open{" +
                "source_node: int32," +
                "target_nodes: [int32]," +
            "weight: [double]" +
            "};" +
            "create dataset Graph(GraphType) primary key source_node;";
    private static final String INIT_BACKBONE_STATEMENT =
            "drop dataverse BackBoneGraph if exists;" +
            "create dataverse BackBoneGraph;" +
            "use dataverse BackBoneGraph;" +
            "create type BackBoneNodeType as open{" +
                "source_node: int32," +
                "target_nodes: [int32]," +
                "weight: [double]" +
            "};" +
            "create dataset BackBoneNode(BackBoneNodeType) primary key source_node";
    private static final String INIT_COMMUNICATION_STATEMENT =
            "drop dataverse Communication if exists;" +
            "create dataverse Communication;" +
            "use dataverse Communication;" +
            "create type ProtocolType as open{" +
                "id: int32," +
                "load_graph: int32," +
                "task1_status: int32," +
                "task2_status: int32," +
                "task3_status: int32," +
                "graph_file_path: string," +
                "number_of_iterations: int32," +
                "source_id: int32," +
                "target_id: int32," +
                "number_of_results: int32" +
            "};" +
            "create dataset Protocol(ProtocolType) primary key id;";
    private static final String INSERT_COMMUNICATION_STATEMENT =
            "use dataverse Communication;" +
            "insert into dataset Protocol" +
            "(" +
            "{\"id\":0," +
                "\"load_graph\":0," +
                "\"task1_status\":0," +
                "\"task2_status\":0," +
                "\"task3_status\":0," +
                "\"graph_file_path\": \"/Users/soushimei/Documents/workspace/Pregelix_Social_Graph/data/CDS/graph_1/\"," +
                "\"number_of_iterations\":10," +
                "\"source_id\":1," +
                "\"target_id\":0," +
                "\"number_of_results\": 5" +
            "}" +
            ");";
    private static final String INIT_TASKS_STATEMENT =
            "drop dataverse Tasks if exists;" +
            "create dataverse Tasks;" +
            "use dataverse Tasks;" +
            "create type TaskOneType as open{" +
                "target_node: int32," +
                "weight: double," +
                "path: [int32]" +
            "};" +
            "create dataset TaskOne(TaskOneType) primary key target_node;" +
            "create type TaskTwoType as open{" +
                "node_id: int32," +
                "community_id: int32" +
            "};" +
            "create dataset TaskTwo(TaskTwoType) primary key node_id;" +
            "create type TaskThreeType as open{" +
                "node_id: int32," +
                "suggested_friends: [int32]" +
            "};" +
            "create dataset TaskThree(TaskThreeType) primary key node_id;";

    public static void initializeAll() throws IOException {
        initializeOriginalGraph();
        initializeBackBoneGraph();
        initializeCommunication();
        initializeTasks();
    }

    public static void initializeOriginalGraph() throws IOException {
        String aql = URLGenerator.cmdParser(INIT_GRAPH_STATEMENT);
        String url = URLGenerator.generate("localhost", 19002, RestAPI.DDL, aql);
        Commander.sendGet(url);
    }

    public static void initializeBackBoneGraph() throws IOException {
        String aql = URLGenerator.cmdParser(INIT_BACKBONE_STATEMENT);
        String url = URLGenerator.generate("localhost", 19002, RestAPI.DDL, aql);
        Commander.sendGet(url);
    }

    public static void initializeCommunication() throws IOException {
        String aql = URLGenerator.cmdParser(INIT_COMMUNICATION_STATEMENT);
        String url = URLGenerator.generate("localhost", 19002, RestAPI.DDL, aql);
        Commander.sendGet(url);

        aql = URLGenerator.cmdParser(INSERT_COMMUNICATION_STATEMENT);
        url = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, aql);
        Commander.sendGet(url);
    }

    public static void initializeTasks() throws IOException {
        String aql = URLGenerator.cmdParser(INIT_TASKS_STATEMENT);
        String url = URLGenerator.generate("localhost", 19002, RestAPI.DDL, aql);
        Commander.sendGet(url);
    }
}