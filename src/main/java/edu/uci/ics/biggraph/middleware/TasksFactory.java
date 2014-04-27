package edu.uci.ics.biggraph.middleware;

/**
 * Created by soushimei on 4/27/14.
 */
public class TasksFactory {
    public static Task createTask(TaskType taskType) {
        Task task = null;
        switch (taskType) {
            case LOAD_GRAPH:
                task = new LoadGraph();
                break;
            default:
                System.err.println("Unknown task type.");
                break;
        }
        return task;
    }
}
