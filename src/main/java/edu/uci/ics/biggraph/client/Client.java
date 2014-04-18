/*
 * Copyright 2009-2013 by The Regents of the University of California
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License from
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.biggraph.client;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import edu.uci.ics.pregelix.api.job.PregelixJob;
import edu.uci.ics.pregelix.core.base.IDriver.Plan;
import edu.uci.ics.pregelix.core.driver.Driver;

import edu.uci.ics.biggraph.algo.SocialSuggestionVertex;
import edu.uci.ics.biggraph.algo.WeightedShortestPathVertex;

public class Client {

    private static class Options {
        @Option(name = "-inputpaths", usage = "comma seprated input paths", required = true)
        public String inputPaths;

        @Option(name = "-outputpath", usage = "output path", required = true)
        public String outputPath;

        @Option(name = "-ip", usage = "ip address of cluster controller", required = true)
        public String ipAddress;

        @Option(name = "-port", usage = "port of cluster controller", required = false)
        public int port;

        @Option(name = "-plan", usage = "query plan choice", required = false)
        public Plan planChoice = Plan.OUTER_JOIN;

        @Option(name = "-vnum", usage = "number of vertices", required = false)
        public long numVertices;

        @Option(name = "-enum", usage = "number of vertices", required = false)
        public long numEdges;

        @Option(name = "-source-vertex", usage = "source vertex id, for shortest paths/reachibility only", required = false)
        public long sourceId;

        @Option(name = "-runtime-profiling", usage = "whether to do runtime profifling", required = false)
        public String profiling = "false";
        
        @Option(name = "-iterations", usage = "maximum # of iterations a vertex will compute for", required = false)
        public int maxIterations;
        
        @Option(name = "-results-num", usage = "(maximum) # of suggestions desired", required = false)
        public int numResults;
    }

    public static void run(String[] args, PregelixJob job) throws Exception {
        Options options = prepareJob(args, job);
        Driver driver = new Driver(Client.class);
        driver.runJob(job, options.planChoice, options.ipAddress, options.port, Boolean.parseBoolean(options.profiling));
    }

    private static Options prepareJob(String[] args, PregelixJob job) throws CmdLineException, IOException {
        Options options = new Options();
        CmdLineParser parser = new CmdLineParser(options);
        parser.parseArgument(args);

        String[] inputs = options.inputPaths.split(";");
        System.out.println("======");
        System.out.println(options.inputPaths);
        System.out.println(options.outputPath);
        System.out.println(options.numVertices);
        System.out.println(options.numEdges);
        System.out.println(options.sourceId);
        System.out.println("======");
        FileInputFormat.setInputPaths(job, inputs[0]);
        for (int i = 1; i < inputs.length; i++)
            FileInputFormat.addInputPaths(job, inputs[0]);
        FileOutputFormat.setOutputPath(job, new Path(options.outputPath));
        job.getConfiguration().setLong(PregelixJob.NUM_VERTICE, options.numVertices);
        job.getConfiguration().setLong(PregelixJob.NUM_EDGES, options.numEdges);
        job.getConfiguration().setLong(WeightedShortestPathVertex.SOURCE_ID, options.sourceId);
        
        // specific for task 3: social suggestion
        job.getConfiguration().setLong(SocialSuggestionVertex.ITERATIONS, options.maxIterations);
        job.getConfiguration().setLong(SocialSuggestionVertex.NUM_RESULTS, options.numResults);
        return options;
    }

}
