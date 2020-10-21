package bunch;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

//import bunch.*;
import bunch.api.*;
import bunch.engine.*;

public class RunBunch {

    public static void main(String[] args) throws Exception {
//        if (args.length != 2) {
//            System.out.println("Usage: java RunBunch <file-name> <dest>");
//            System.exit(1);
//        }


        /*
         * For each relationshipType in relationships:
         *   set relationshipType.weight = 100%
         *   produce weighted graph
         *   relationshipMQValue = Bunch.getMQValue()
         *   if(MQValue == 1.0):
         *    disregardedRelationships.add(relationshipType)
         *
         *    highestMQValue = 1;
         *    highestMoJoFMValue = 1;
         *    bestResults = [];
         *
         * list = Generate all possible lists of length (len(relationships - disregardedRelationships)) that sum to 100
         * for each row in list:
         * for relationshipType in relationshipTypes:
         *  i = len(row)
         *  while i >= 0:
         * if relationshipType in disregardedRelationshipTypes:
         *  continue
         * else:
         * relationshipType.weight = row[i]
         * i--
         * [outputGraph, MQ, MoJoFMD] = generateWeightedGraph(weights)
         * if MQ > highestMQValue:
         *  bestResults = outputGraph
         *  highestMQValue = MQ
         * else:
         */

        //System.out.println(args[0]);

        BunchAPI api = new BunchAPI();
        BunchProperties bp = new BunchProperties();
        String inputFile = args[0];
        String outputFile = args[1];

        System.out.println(args[0]);
        System.out.println(args[1]);

        //bp.setProperty(BunchProperties.MDG_INPUT_FILE_NAME, args[0]);

        bp.setProperty(BunchProperties.MDG_INPUT_FILE_NAME, inputFile);
        bp.setProperty(BunchProperties.OUTPUT_FORMAT, BunchProperties.TEXT_OUTPUT_FORMAT);
        //bp.setProperty(BunchProperties.OUTPUT_DIRECTORY, args[1]);
        bp.setProperty(BunchProperties.OUTPUT_DIRECTORY, outputFile);
        bp.setProperty(BunchProperties.ECHO_RESULTS_TO_CONSOLE, "True");

        bp.setProperty(BunchProperties.MQ_CALCULATOR_CLASS, "bunch.TurboMQ");
        bp.setProperty(BunchProperties.CLUSTERING_APPROACH, BunchProperties.AGGLOMERATIVE);

        //added
        bp.setProperty(BunchProperties.MDG_OUTPUT_MODE, BunchProperties.OUTPUT_TREE);
        bp.setProperty(BunchProperties.ALG_HC_POPULATION_SZ, "100");
        bp.setProperty(BunchProperties.ALG_HC_HC_PCT, "50");
        bp.setProperty(BunchProperties.ALG_HC_RND_PCT, "100");
        if(args.length == 3)
        {
            System.out.println(args[2]);
            bp.setProperty(BunchProperties.FIX_NUMBER_OF_CLUSTERS, "True");
            bp.setProperty(BunchProperties.NUMBER_OF_CLUSTERS, args[2]);
        }
        else
        {
            bp.setProperty(BunchProperties.FIX_NUMBER_OF_CLUSTERS, "False");
        }

        api.setProperties(bp);
        api.run();

        Hashtable results = api.getResults();
        int medLvlGraphIndex = Integer.parseInt((String) results.get("MedianLevelGraph"));
        Hashtable [] resultLevels = (Hashtable[])results.get(BunchAPI.RESULT_CLUSTER_OBJS);

        String MQResult = (String) ((Hashtable) resultLevels[medLvlGraphIndex]).get(BunchAPI.MQVALUE);
        System.out.println(MQResult);

        //Output detailed information for each level
        for(int i = 0; i < resultLevels.length; i++)
        {
            Hashtable lvlResults = resultLevels[i];
            System.out.println("***** LEVEL "+i+"*****");
            String mq = (String)lvlResults.get(BunchAPI.MQVALUE);
            String depth = (String)lvlResults.get(BunchAPI.CLUSTER_DEPTH);
            String numC = (String)lvlResults.get(BunchAPI.NUMBER_CLUSTERS);
            System.out.println(" MQ Value = " + mq);
            System.out.println(" Best Cluster Depth = " + depth);
            System.out.println(" Number of Clusters in Best Partition = " +
                    numC);
            System.out.println();
        }

        BunchEngine engine = null;
        Field field = api.getClass().getDeclaredField("engine");
        field.setAccessible(true);
        engine = (BunchEngine) field.get(api);
        double MQVal = engine.getBestGraph().getObjectiveFunctionValue();

        GraphOutput graphOutput = new GraphOutputFactory().getOutput("Text");
        graphOutput.setBaseName(inputFile);
        graphOutput.setBasicName(inputFile);
        String outputFileName = graphOutput.getBaseName();
        String outputPath = outputFile;
        if (outputPath != null) {
            File f = new File(graphOutput.getBaseName());
            String filename = f.getName();
            outputFileName = outputPath + filename;
        }
        graphOutput.setCurrentName(outputFileName);
        graphOutput.setOutputTechnique(3);
        graphOutput.setGraph(engine.getBestGraph());
        graphOutput.write();
    }
}
