/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.core;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.EngineException;
import static fr.inria.edelweiss.kgdqp.core.CentralizedInferrencingNoSpin.logger;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.util.SPINProcess;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class CentralizedInferrencingNoSpin {

    static Logger logger = Logger.getLogger(CentralizedInferrencingNoSpin.class);

    static final String pertinentRulesQueryOumy = "prefix sp: <http://spinrdf.org/sp#>\n"
            + "Select   DISTINCT  (st:apply-templates-with(st:spin, ?r) as ?res) \n"
            + "     WHERE{ \n"
            + "   ?a a sp:Construct\n"
            + "             ?a sp:where ?m\n"
            + "             ?m (!sp:nil)+ ?x\n"
            + "             ?x ?p ?resource\n"
            + "                VALUES ?p { sp:subject sp:predicate sp:object }\n"
            + "\n"
            + "   OPTIONAL{ ?a sp:templates ?t\n"
            + "             ?t (!sp:nil)+ ?x3\n"
            + "             ?x3 ?p3 ?r2\n"
            + "                VALUES ?p3 { sp:subject sp:predicate sp:object }\n"
            + "             ?a2 a sp:Construct\n"
            + "             ?a2 sp:where ?m2\n"
            + "             ?m2 (!sp:nil)+ ?x2\n"
            + "             ?x2 ?p2 ?r2\n"
            + "                VALUES ?p2 {sp:subject sp:predicate sp:object }\n"
            + "             Filter (strstarts(?r2,owl:)||strstarts(?r2,rdfs:))\n"
            + "        }\n"
            + "{      SELECT Distinct ?resource  \n"
            + "             WHERE{     \n"
            + "            {?resource a rdfs:Resource} union {?y a ?resource}\n"
            + "              Filter (strstarts(?resource,owl:)||strstarts(?resource,rdfs:))\n"
            + "             } \n"
            + "}\n"
            + "     \n"
            + "  ?r a sp:Construct \n"
            + "  Filter ( ?r = ?a || ?r = ?a2)\n"
            + "}";

    static final String pertinentRulesQuery = "prefix sp: <http://spinrdf.org/sp#>\n"
            + "Select DISTINCT  (st:apply-templates-with(st:spin, ?r) as ?res)\n"
            + "WHERE {\n"
            + "{\n"
            + "# URI in ontology from owl: and rdfs:\n"
            + "  SELECT Distinct ?resource\n"
            + "       WHERE {    \n"
            + "         graph ?g {\n"
            + "         ?o a owl:Ontology\n"
            + "	  { ?resource ?p ?y} union {?x ?resource ?y} union {?x ?p ?resource}\n"
            + "	    filter (\n"
            + "	    (?resource != owl:members)\n"
            + "	    && (\n"
            + "	    ?resource in (rdf:Property)\n"
            + "	     || strstarts(?resource, owl:) \n"
            + "	     || strstarts(?resource, rdfs:))\n"
            + "	    )                    \n"
            + "	  }\n"
            + "#\n"
            + "# filter(strcontains(?g, \"onto\"))\n"
            + "#\n"
            + "         }\n"
            + "}\n"
            + "\n"
            + "?a a sp:Construct\n"
            + "          ?a sp:where ?m\n"
            + "          ?m (!sp:nil)+ ?x\n"
            + "          ?x ?p ?resource\n"
            + "VALUES ?p { sp:subject sp:predicate sp:object }\n"
            + "          \n"
            + "OPTIONAL{                    \n"
            + "          ?a2 a sp:Construct\n"
            + "          ?a2 sp:where ?w\n"
            + "          \n"
            + "          filter not exists {\n"
            + "	    ?w (!sp:nil)+ ?uri .\n"
            + "	    \n"
            + "	    filter(isURI(?uri)) \n"
            + "	    filter (\n"
            + "	    (?uri != owl:members) && (?uri != owl:sameAs)\n"
            + "	    && (\n"
            + "	    ?uri in (rdf:Property)\n"
            + "	     || strstarts(?uri, owl:) \n"
            + "	     || strstarts(?uri, rdfs:))\n"
            + "	    )     \n"
            + "	    \n"
            + "	    filter not exists {\n"
            + "		    graph ?g {\n"
            + "		?o a owl:Ontology\n"
            + "		{ ?uri ?p ?y} union {?x ?uri ?y} union {?x ?p ?uri}	                  \n"
            + "		}\n"
            + "# \n"
            + "# filter(strcontains(?g, \"onto\"))\n"
            + "#		\n"
            + "         }\n"
            + "	      }\n"
            + "                      \n"
            + "           \n"
            + "	  ?a sp:templates ?t\n"
            + "          ?t (!sp:nil)+ ?t1 .          \n"
            + "          ?t1 sp:subject ?s1 ;\n"
            + "	    sp:object ?o1 ;\n"
            + "	    sp:predicate ?p1 .\n"
            + "	    \n"
            + "	    optional { ?s1 sp:varName ?ns1 }\n"
            + "            optional { ?p1 sp:varName ?np1 }\n"
            + "            optional { ?o1 sp:varName ?no1 } \n"
            + "          \n"
            + "          ?w (!sp:nil)+ ?t2 .\n"
            + "          \n"
            + "          ?t2 sp:subject ?s2 ;\n"
            + "	    sp:object ?o2 ;\n"
            + "	    sp:predicate ?p2 .\n"
            + "	    \n"
            + "	  optional { ?s2 sp:varName ?ns2 }\n"
            + "          optional { ?p2 sp:varName ?np2 }\n"
            + "          optional { ?o2 sp:varName ?no2 }\n"
            + "          \n"
            + "	  filter(?s1 = ?s2 || bound(?ns1) || bound (?ns2))\n"
            + "	  filter(?p1 = ?p2 || bound(?np1) || bound (?np2))\n"
            + "	  filter(?o1 = ?o2 || bound(?no1) || bound (?no2))\n"
            + "	  	             \n"
            + "           }\n"
            + "\n"
            + "?r a sp:Construct\n"
            + "Filter ( ?r = ?a || ?r = ?a2)\n"
            + "}\n"
            + "pragma {kg:path kg:list true}";

    static final String allRulesQuery = "prefix sp: <http://spinrdf.org/sp#>\n"
            + "Select   DISTINCT  (st:apply-templates-with(st:spin, ?r) as ?res) \n"
            + "     WHERE{ \n"
            + "   ?r a sp:Construct\n"
            + "}";

    public static void main(String args[]) throws ParseException, EngineException, InterruptedException, IOException, LoadException {

        List<String> endpoints = new ArrayList<String>();
        String queryPath = null;
        boolean rulesSelection = false;
        File rulesDir = null;
        File ontDir = null;

        /////////////////
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        Options options = new Options();
        Option helpOpt = new Option("h", "help", false, "print this message");
//        Option queryOpt = new Option("q", "query", true, "specify the sparql query file");
//        Option endpointOpt = new Option("e", "endpoint", true, "a federated sparql endpoint URL");
        Option versionOpt = new Option("v", "version", false, "print the version information and exit");
        Option rulesOpt = new Option("r", "rulesDir", true, "directory containing the inference rules");
        Option ontOpt = new Option("o", "ontologiesDir", true, "directory containing the ontologies for rules selection");
//        Option locOpt = new Option("c", "centralized", false, "performs centralized inferences");
        Option dataOpt = new Option("l", "load", true, "data file or directory to be loaded");
//        Option selOpt = new Option("s", "rulesSelection", false, "if set to true, only the applicable rules are run");
//        options.addOption(queryOpt);
//        options.addOption(endpointOpt);
        options.addOption(helpOpt);
        options.addOption(versionOpt);
        options.addOption(rulesOpt);
        options.addOption(ontOpt);
//        options.addOption(selOpt);
//        options.addOption(locOpt);
        options.addOption(dataOpt);

        String header = "Corese/KGRAM rule engine experiment command line interface";
        String footer = "\nPlease report any issue to alban.gaignard@cnrs.fr, olivier.corby@inria.fr";

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("kgdqp", header, options, footer, true);
            System.exit(0);
        }
        if (cmd.hasOption("o")) {
            rulesSelection = true;
            String ontDirPath = cmd.getOptionValue("o");
            ontDir = new File(ontDirPath);
            if (!ontDir.isDirectory()) {
                logger.warn(ontDirPath + " is not a valid directory path.");
                System.exit(0);
            }
        }
        if (!cmd.hasOption("r")) {
            logger.info("You must specify a path for inference rules directory !");
            System.exit(0);
        }

        if (cmd.hasOption("l")) {
            String[] dataPaths = cmd.getOptionValues("l");
            for (String path : dataPaths) {
                Load ld = Load.create(graph);
                ld.load(path);
                logger.info("Loaded " + path);
            }
        }

        if (cmd.hasOption("v")) {
            logger.info("version 3.0.4-SNAPSHOT");
            System.exit(0);
        }

        String rulesDirPath = cmd.getOptionValue("r");
        rulesDir = new File(rulesDirPath);
        if (!rulesDir.isDirectory()) {
            logger.warn(rulesDirPath + " is not a valid directory path.");
            System.exit(0);
        }

        // Local rules graph initialization
        Graph rulesG = Graph.create();
        Load ld = Load.create(rulesG);

        if (rulesSelection) {
            // Ontology loading
            if (ontDir.isDirectory()) {
                for (File o : ontDir.listFiles()) {
                    logger.info("Loading " + o.getAbsolutePath());
                    ld.load(o.getAbsolutePath());
                }
            }
        }

        // Rules loading
        if (rulesDir.isDirectory()) {
            for (File r : rulesDir.listFiles()) {
                if (r.getAbsolutePath().endsWith(".rq")) {
                    logger.info("Loading " + r.getAbsolutePath());
//                ld.load(r.getAbsolutePath());

                    byte[] encoded = Files.readAllBytes(Paths.get(r.getAbsolutePath()));
                    String construct = new String(encoded, StandardCharsets.UTF_8);
                    SPINProcess sp = SPINProcess.create();
                    String spinConstruct = sp.toSpin(construct);
                    
                    ld.load(new ByteArrayInputStream(spinConstruct.getBytes()), Load.TURTLE_FORMAT);
                    logger.info("Rules graph size : " + rulesG.size());
                }
            }
        }

        // Rule engine initialization
        RuleEngine ruleEngine = RuleEngine.create(graph);
        ruleEngine.set(exec);
        ruleEngine.setOptimize(true);
        ruleEngine.setConstructResult(true);
        ruleEngine.setTrace(true);

        StopWatch sw = new StopWatch();
        logger.info("Federated graph size : " + graph.size());
        logger.info("Rules graph size : " + rulesG.size());

        // Rule selection
        logger.info("Rules selection");
        QueryProcess localKgram = QueryProcess.create(rulesG);
        ArrayList<String> applicableRules = new ArrayList<String>();
        sw.start();
        String rulesSelQuery = "";
        if (rulesSelection) {
            rulesSelQuery = pertinentRulesQuery;
        } else {
            rulesSelQuery = allRulesQuery;
        }
        Mappings maps = localKgram.query(rulesSelQuery);
        logger.info("Rules selected in " + sw.getTime() + " ms");
        logger.info("Applicable rules : " + maps.size());

        // Selected rule loading
        for (Mapping map : maps) {
            IDatatype dt = (IDatatype) map.getValue("?res");
            String rule = dt.getLabel();
            //loading rule in the rule engine
//            logger.info("Adding rule : ");
//            System.out.println("-------");
//            System.out.println(rule);
//            System.out.println("");
//            if (! rule.toLowerCase().contains("sameas")) {
            applicableRules.add(rule);
            ruleEngine.addRule(rule);
//            }
        }

        // Rules application on distributed sparql endpoints
        logger.info("Rules application (" + applicableRules.size() + " rules)");
        ExecutorService threadPool = Executors.newCachedThreadPool();
        RuleEngineThread ruleThread = new RuleEngineThread(ruleEngine);
        sw.reset();
        sw.start();

//        ruleEngine.process();
        threadPool.execute(ruleThread);
        threadPool.shutdown();

        //monitoring loop
        while (!threadPool.isTerminated()) {
//            System.out.println("******************************");
//            System.out.println(Util.jsonDqpCost(QueryProcessDQP.queryCounter, QueryProcessDQP.queryVolumeCounter, QueryProcessDQP.sourceCounter, QueryProcessDQP.sourceVolumeCounter));
//            System.out.println("Rule engine running for " + sw.getTime() + " ms");
//            System.out.println("Federated graph size : " + graph.size());
            System.out.println(sw.getTime() + " , " + graph.size());
            Thread.sleep(5000);
        }

        logger.info("Federated graph size : " + graph.size());
//        logger.info(Util.jsonDqpCost(QueryProcessDQP.queryCounter, QueryProcessDQP.queryVolumeCounter, QueryProcessDQP.sourceCounter, QueryProcessDQP.sourceVolumeCounter));

//        TripleFormat f = TripleFormat.create(graph, true);
//        f.write("/tmp/gAll.ttl");
    }
}
