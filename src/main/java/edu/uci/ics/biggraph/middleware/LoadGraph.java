package edu.uci.ics.biggraph.middleware;

import edu.uci.ics.biggraph.servlet.ProtocolTypeAccessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by soushimei on 4/27/14.
 */
public class LoadGraph extends Task {
    public LoadGraph() {
        super(TaskType.LOAD_GRAPH);
    }

    @Override
    public void runTask(String pregelixPath, String projectPath, String port) {
        // get the configuration
        getConfiguration(pregelixPath, projectPath, port);

        // get the command based on the configuration
        command = getCommand();

        // run the command
        try {
            runCommand();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getConfiguration(String pregelixPath, String projectPath, String port) {
        // set by user's configuration
        this.pregelixPath = pregelixPath;
        this.projectPath = projectPath;
        this.port = port;

        // set by default
        this.taskClass = "edu.uci.ics.biggraph.algo.SpanningTreeVertex";
        this.outputPath = "/tmp/pregelix_result";
        this.ip = "`bin/getip.sh`";

        // set by querying the database
//        inputGraphPath = getInputGraphPath();
        inputGraphPath = ProtocolTypeAccessor.getInstance().getGraphFilePath();
    }

    @Override
    public String getCommand() {
        // build the command
        StringBuffer stringBuffer = new StringBuffer();
        // append pregelix path
        stringBuffer.append("bin/pregelix ");
        // append project path
        stringBuffer.append(projectPath).append(" ");
        // append task class name
        stringBuffer.append(taskClass).append(" ");
        // append input path
        stringBuffer.append("-inputpaths ").append(inputGraphPath).append(" ");
        // append output path
        stringBuffer.append("-outputpath ").append(outputPath).append(" ");
        // append ip configuration
        stringBuffer.append("-ip ").append(ip).append(" ");
        // append port configuration
        stringBuffer.append("-port ").append(port);

        return stringBuffer.toString();
    }

    private void runCommand() throws IOException, InterruptedException {
        System.out.println("Executing " + command);
        // writing scripts to the pregelix path
        File file = new File(pregelixPath + "load_path.sh");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(command);
        fileWriter.close();

        // change mode to the executive
        Process changeMode = Runtime.getRuntime().exec("chmod a+x " + pregelixPath + "load_path.sh");
        changeMode.waitFor();

        // run the command
        System.setProperty("user.dir", pregelixPath);
        Process p = Runtime.getRuntime().exec(pregelixPath + "load_path.sh", null, new File(pregelixPath));
        p.waitFor();
    }

    private String command = null;
}
