package edu.uci.ics.biggraph.middleware;

import java.io.IOException;

/**
 * Created by soushimei on 4/27/14.
 */
public abstract class Task {
    private TaskType taskType = null;

    public Task(TaskType taskType) {
        this.taskType = taskType;
    }

    public abstract void runTask(String pregelixPath, String projectPath, String port);
    public abstract void getConfiguration(String pregelixPath, String projectPath, String port);
    public abstract String getCommand();

    public TaskType getTaskType() {
        return this.taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }


    protected String inputGraphPath = null;
    protected String getInputGraphPath() {
        try {
            inputGraphPath = ProtocolOperation.getInputGraphPath();
        } catch (IOException e) {
            e.printStackTrace();
            inputGraphPath = "/Users/soushimei/Documents/workspace/Pregelix_Social_Graph/data/CDS/graph_1/";
        }
//        inputGraphPath = "/Users/soushimei/Documents/workspace/Pregelix_Social_Graph/data/CDS/graph_1/";
        return inputGraphPath;
    }
}
