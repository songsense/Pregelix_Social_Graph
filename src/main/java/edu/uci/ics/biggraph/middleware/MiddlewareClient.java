package edu.uci.ics.biggraph.middleware;

import edu.uci.ics.biggraph.servlet.DatabaseInitializer;
import edu.uci.ics.biggraph.servlet.ProtocolTypeAccessor;
import org.kohsuke.args4j.CmdLineException;

import java.io.IOException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
/**
 * Created by soushimei on 4/27/14.
 */
public class MiddlewareClient {
    private static final int sleepTime = 1000;
    private static Options options = new Options();
    private static TaskType taskType = TaskType.NOP;


    private static class Options {
        @Option(name = "-preglix_path", usage = "pregelix path", required = true)
        public String pregelixPath;
        @Option(name = "-project_path", usage = "project path", required = true)
        public String projectPath;
        @Option(name = "-port", usage = "port number", required = true)
        public String port;

    }
    public static void run(String[] args) throws IOException, CmdLineException, InterruptedException {
        options = prepareJob(args);
        System.out.println("====== Middleware of Pregelix and AsterixDB started ======");
        System.out.println("Pregelix path is configured as " + options.pregelixPath);
        System.out.println("Project path is configured as " + options.projectPath);
        System.out.println("Port number is configured as " + options.port);
        runJob();
    }

    private static Options prepareJob(String[] args)  throws CmdLineException, IOException {
        CmdLineParser parser = new CmdLineParser(options);
        parser.parseArgument(args);
        return options;
    }

    private static TaskType getTaskType() {
        taskType = TaskType.NOP;
        if (ProtocolTypeAccessor.getInstance().getLoadGraphStatus() == 1) {
            taskType = TaskType.LOAD_GRAPH;
        } else if (ProtocolTypeAccessor.getInstance().getTaskStatus(1) == 1) {
            taskType = TaskType.TASK_1;
        } else if (ProtocolTypeAccessor.getInstance().getTaskStatus(2) == 1) {
            taskType = TaskType.TASK_2;
        } else if (ProtocolTypeAccessor.getInstance().getTaskStatus(3) == 1) {
            taskType = TaskType.TASK_3;
        }
        return taskType;
    }

    private static void setTaskComplete() throws IOException {
        switch (taskType) {
            case TASK_1:
                ProtocolTypeAccessor.getInstance().setTaskStatus(1,2);
                ProtocolTypeAccessor.getInstance().storeEntry();
                break;
            case TASK_2:
                ProtocolTypeAccessor.getInstance().setTaskStatus(2,2);
                ProtocolTypeAccessor.getInstance().storeEntry();
                break;
            case TASK_3:
                ProtocolTypeAccessor.getInstance().setTaskStatus(3,2);
                ProtocolTypeAccessor.getInstance().storeEntry();
                break;
            case LOAD_GRAPH:
                ProtocolTypeAccessor.getInstance().setLoadGraphStatus(2);
                ProtocolTypeAccessor.getInstance().storeEntry();
                break;
        }

    }

    private static void runJob() throws InterruptedException, IOException {
        Task task;

        DatabaseInitializer.initializeAll();



//        task = TasksFactory.createTask(TaskType.LOAD_GRAPH);
//        task.runTask(options.pregelixPath, options.projectPath, options.port);
//        System.out.println("Finish Load Graph");

//        task = TasksFactory.createTask(TaskType.TASK_1);
//        task.runTask(options.pregelixPath, options.projectPath, options.port);
//        System.out.println("Finish Task 1");
//
//        task = TasksFactory.createTask(TaskType.TASK_2);
//        task.runTask(options.pregelixPath, options.projectPath, options.port);
//        System.out.println("Finish Task 2");

        while(true) {
            Thread.sleep(sleepTime);
            ProtocolTypeAccessor.getInstance().loadEntry();

            // get the task type
            getTaskType();

            // run the task
            switch (taskType) {
                case TASK_1:
                    task = TasksFactory.createTask(TaskType.TASK_1);
                    task.runTask(options.pregelixPath, options.projectPath, options.port);
                    System.out.println("Finish Task 1");
                    break;
                case TASK_2:
                    task = TasksFactory.createTask(TaskType.TASK_2);
                    task.runTask(options.pregelixPath, options.projectPath, options.port);
                    System.out.println("Finish Task 2");
                    break;
                case TASK_3:
                    task = TasksFactory.createTask(TaskType.TASK_3);
                    task.runTask(options.pregelixPath, options.projectPath, options.port);
                    System.out.println("Finish Task 3");
                    break;
                case LOAD_GRAPH:
                    task = TasksFactory.createTask(TaskType.LOAD_GRAPH);
                    task.runTask(options.pregelixPath, options.projectPath, options.port);
                    System.out.println("Finish Load Graph");
                    break;
                default:
                    break;
            }

            // set the task to be completed
            setTaskComplete();
        }
    }

    public static void main(String[] args) throws Exception {
        run(args);
    }

}
