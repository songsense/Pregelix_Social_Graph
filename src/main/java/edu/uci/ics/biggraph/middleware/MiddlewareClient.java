package edu.uci.ics.biggraph.middleware;

import org.kohsuke.args4j.CmdLineException;

import java.io.IOException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
/**
 * Created by soushimei on 4/27/14.
 */
public class MiddlewareClient {
    private static final int sleepTime = 500;
    private static Options options = new Options();


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

    private static void runJob() throws InterruptedException {
        Task task = TasksFactory.createTask(TaskType.LOAD_GRAPH);
        task.runTask(options.pregelixPath, options.projectPath, options.port);
        System.out.println("Finish Load Graph");

        task = TasksFactory.createTask(TaskType.TASK_1);
        task.runTask(options.pregelixPath, options.projectPath, options.port);
        System.out.println("Finish Task 1");
//        while(true) {
//            Thread.sleep(sleepTime);
//        }
    }

    public static void main(String[] args) throws Exception {
        run(args);
    }

}
