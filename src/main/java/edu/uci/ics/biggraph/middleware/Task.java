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

    protected String pregelixPath = null;
    protected String projectPath = null;
    protected String taskClass = null;
    protected String inputGraphPath = null;
    protected String outputPath = null;
    protected String ip = null;
    protected String port = null;
}
