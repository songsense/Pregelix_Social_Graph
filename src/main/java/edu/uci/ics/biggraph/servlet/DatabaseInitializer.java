package edu.uci.ics.biggraph.servlet;

/**
 * Created by liqiangw on 4/28/14.
 */
public class DatabaseInitializer {
    private static final String graph =
            "drop dataverse OriginalGraph if exists;" +
            "create dataverse OriginalGraph;" +
            "use dataverse OriginalGraph;" +
            "create type GraphType as open{" +
                "source_node: int32," +
                "target_nodes: [int32]," +
            "weight: [double]" +
            "}" +
            "create dataset Graph(GraphType) primary key source_node;";
    private static final String backbone = null;
    private static final String communication = null;
    private static final String tasks = null;

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
