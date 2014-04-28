package edu.uci.ics.biggraph.servlet;

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
            "}" +
            "create dataset Graph(GraphType) primary key source_node;";
    private static final String INIT_BACKBONE_STATEMENT =
            "drop dataverse BackBoneGraph if exists;" +
            "create dataverse BackBoneGraph;" +
            "use dataverse BackBoneGraph;" +
            "create type BackBoneNodeType as open{" +
                "source_node: int32," +
                "target_nodes: [int32]," +
            "}" +
            "create dataset BackBoneNode(BackBoneNodeType) primary key source_node";
    private static final String INIT_COMMUNICATION_STATEMENT =
            "drop dataverse Communication if exists;\n" +
            "create dataverse Communication;" +
            "use dataverse Communication;" +
            "create type ProtocolType as open {" +
                "id: int32," +
                "load_graph: int32," +
                "task1_status: int32," +
                "task2_status: int32," +
                "task3_status: int32," +
                "graph_file_path: string" +
                "number_of_iterations: int32," +
                "source_id: int32," +
                "target_id: int32," +
                "number_of_results: int32" +
            "}" +
            "create dataset Protocol(ProtocolType) primary key id;\n";
    private static final String INSERT_COMMUNICATION_STATEMENT = null;
    private static final String INIT_TASKS_STATEMENT = null;

    public void initializeAll() {

    }

    public void initializeOriginalGraph() {

    }

    public void initializeBackBoneGraph() {

    }

    public void initializeCommunication() {

    }

    public void initializeTasks() {

    }
}
