package no.sintef.skytrack.owlapi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NullReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;


import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;


public class EvaluationFactpp {
	
	static Logger logger =  LogManager.getRootLogger();
	
	static OWLReasonerConfiguration reasonerConfiguration;

	public static void main(String[] args) {

		Map<String, String> ontologiesMap = new LinkedHashMap<String, String>();

		Options options = new Options();

		Option input = new Option("i", "input", true, "input ontologies folder");
		input.setRequired(false);
		options.addOption(input);

		Option output = new Option("o", "output", true, "output folder");
		output.setRequired(false);
		options.addOption(output);

		Option reasoners = new Option("r", "reasoner", true, "list of reasoner to evaluate (HermiT, JFact, Pellet, Konclude)");
		reasoners.setRequired(false);
		reasoners.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(reasoners);

		Option tasks = new Option("t", "task", true, "list of reasoner tasks to evaluate (loading, consistency, classification, realization)");
		tasks.setRequired(false);
		tasks.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(tasks);

		Option iterations = new Option("n", "iterations", true, "number of iterations for each evaluation");
		iterations.setRequired(false);
		options.addOption(iterations);

		Option printOnt = new Option("p", "print", false, "list of the ontologies to be evaluated");
		printOnt.setRequired(false);
		options.addOption(printOnt);
		
		
		Option fileOnt = new Option("f", "file", true, "print statistics of ontologies");
		fileOnt.setRequired(false);
		fileOnt.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(fileOnt);
		
		Option jump = new Option("j", "jump", true, "jump to current ontology");
		jump.setRequired(false);
		options.addOption(jump);
		
		Option skipOpt = new Option("s", "skip", true, "Skip those ontologies");
		skipOpt.setRequired(false);
		skipOpt.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(skipOpt);
		
		
		

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;// not a good practice, it serves it purpose

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.toString());
			formatter.printHelp("Evaluation", options);

			System.exit(1);
		}

		
		String inputFilePath = ".";
		
		if(!cmd.hasOption("file"))
		{
			
			//------------------------------------------------------
			// Input Dir
			//------------------------------------------------------
			
			
			inputFilePath = cmd.getOptionValue("input");
			if (inputFilePath == null)
				inputFilePath = "../../../ontologies/ontologies";
			File inputDir = new File(inputFilePath);
			if (inputDir.exists()) {
				File[] listOfFiles = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith("owl")
						|| name.toLowerCase().endsWith("ttl") || name.toLowerCase().endsWith("xml"));

				Arrays.sort(listOfFiles, Comparator.comparingLong(File::length));
				for (File file : listOfFiles)
					if (file.isFile()) {
						ontologiesMap.put(file.getName(), file.getAbsolutePath());
					}

				if (ontologiesMap.isEmpty()) {
					logger.error("Input Dir: " + inputFilePath + " is empty");
					System.exit(0);
				}

			} else {
				logger.error("Input Dir: " + inputFilePath + " not exist");
				System.exit(0);
			}
			
			
			//------------------------------------------------------
			// Jump to ontology
			//------------------------------------------------------
			if(cmd.hasOption("jump"))
			{
				String ontoToJump = cmd.getOptionValue("jump");
				if(ontologiesMap.containsKey(ontoToJump.trim()))
				{
					ArrayList<String> keySet = new ArrayList<String>(ontologiesMap.keySet());
					for(String name : keySet)
					{
						if(name.equals(ontoToJump.trim()))
							break;
						
						ontologiesMap.remove(name);
					}
				}
			}
			
			
			//------------------------------------------------------
			// Skip ontologies
			//------------------------------------------------------
			
			if(cmd.hasOption("skip"))
			{
				String[] files = cmd.getOptionValues("skip");
			
				for(String name : files)
				{
					if(ontologiesMap.containsKey(name.trim()))
						ontologiesMap.remove(name);
					else
						logger.info("File " + name + " does not exist in the input path");
				}
			}
			
		}
		
		

		
		//------------------------------------------------------
		// File option
		//------------------------------------------------------
		if(cmd.hasOption("file"))
		{
			ontologiesMap.clear();
			String[] files = cmd.getOptionValues("file");
			for(String name : files)
			{
				File file = new File(name); 
				if(!file.exists())
				{
					logger.info("File " + file.getAbsolutePath() + " does not exist.");
					continue;
				}
				if (file.exists() && file.isFile()) {
					ontologiesMap.put(file.getName(), file.getAbsolutePath());
				}
			}
			
		}


		
		//------------------------------------------------------
		// Output Dir
		//------------------------------------------------------
		String outputFilePath = cmd.getOptionValue("output");
		if (outputFilePath == null)
			outputFilePath = "./output";
		File outputDir = new File(outputFilePath);
		if (!outputDir.exists())
			outputDir.mkdirs();
		
		
		//------------------------------------------------------
		// Reasoners
		//------------------------------------------------------

		ArrayList<String> supportReasoners = new ArrayList<String>(
				Arrays.asList("Factpp", "Pellet"));
		String[] reasonersName = cmd.getOptionValues("reasoner");
		if (reasonersName == null) {
			reasonersName = new String[] {};
			reasonersName = supportReasoners.toArray(reasonersName);
		} else {
			for (String name : reasonersName) {
				if (!supportReasoners.contains(name)) {
					logger.error("Reasoner: " + name + " is not supported");
					System.exit(0);
				}
			}
		}

		
		
		//------------------------------------------------------
		// Tasks
		//------------------------------------------------------
		
		ArrayList<String> supportTasks = new ArrayList<String>(
				Arrays.asList("loading", "consistency", "classification", "realization"));

		String[] tasksName = cmd.getOptionValues("task");
		if (tasksName == null) {
			tasksName = new String[] {};
			tasksName = supportTasks.toArray(tasksName);
		} else {
			for (String task : tasksName) {
				if (!supportTasks.contains(task)) {
					logger.error("Task: " + task + " is not supported");
					System.exit(0);
				}
			}
		}

		
		
		//------------------------------------------------------
		// iterations
		//------------------------------------------------------
		
		Integer runs = null;

		if(cmd.hasOption("iterations"))
		{
			try {
				runs = Integer.valueOf(cmd.getOptionValue("iterations"));
			} catch (Exception e1) {
				logger.info(e1.toString());
				runs = 10;

			}
		}

		if (runs == null)
			runs = 10;
		
		//------------------------------------------------------
		// Printing args
		//------------------------------------------------------
		
		/*logger.info("");
		logger.info("--------------------------------------------------");
		logger.info("");
		

		logger.info("inputFilePath: " + inputFilePath);
		logger.info("outputFilePath: " + outputFilePath);
		logger.info("iterations: " + runs);
		logger.info("reasoner: " + Arrays.toString(reasonersName));
		logger.info("task: " + Arrays.toString(tasksName));
		logger.info("ontologies: " + Arrays.toString(ontologiesMap.keySet().toArray()));

		logger.info("");
		logger.info("--------------------------------------------------");
		logger.info("");*/
		
		
		
		//------------------------------------------------------
		// Create reasoners
		//------------------------------------------------------

		Map<String, OWLReasonerFactory> reasonerFactoryMap = new LinkedHashMap<>();

		ArrayList<String> reasonersNameList = new ArrayList<String>(Arrays.asList(reasonersName));
		if (reasonersNameList.contains("Factpp"))
			reasonerFactoryMap.put("Factpp", new uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory());
		
		if (reasonersNameList.contains("Pellet"))
			reasonerFactoryMap.put("Pellet", new PelletReasonerFactory());
		
		//if (reasonersNameList.contains("Sequoia"))
		//	reasonerFactoryMap.put("Sequoia", new SequoiaReasonerFactory());
	
		
		//------------------------------------------------------
		// Create reasoners Configuration
		//------------------------------------------------------
		
		long reasonerTimeOut = 2*60*60*1000;
		
		try {
			
			reasonerConfiguration = new OWLReasonerConfiguration() {
				
				/**
				 * 
				 */
				private static final long serialVersionUID = -9157183864788821701L;

				@Override
				public long getTimeOut() {
					return reasonerTimeOut;
				}
				
				@Override
				public ReasonerProgressMonitor getProgressMonitor() {
					return new NullReasonerProgressMonitor();
				}
				
				@Override
				public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
					return IndividualNodeSetPolicy.BY_NAME;
				}
				
				@Override
				public FreshEntityPolicy getFreshEntityPolicy() {
					
					return FreshEntityPolicy.ALLOW;
				}
			};
			  
		  
		  
		} catch (Exception e2) {
			e2.printStackTrace();
			System.exit(0);
		}
		
		
		
		
		//------------------------------------------------------
		// Task Load Reasoners
		//------------------------------------------------------
		
		ArrayList<String> taskNameList = new ArrayList<String>(Arrays.asList(tasksName));
		
		if(taskNameList.contains("loading"))
		{
			//logger.info("");
			logger.info("--------------------------------------------------");
			//logger.info("");
			taskLoadReasoner(ontologiesMap, reasonerFactoryMap, outputFilePath, runs);
		}
		//------------------------------------------------------
		// Task Consistency
		//------------------------------------------------------
		
		if(taskNameList.contains("consistency"))
		{
			//logger.info("");
			logger.info("--------------------------------------------------");
			//logger.info("");
			taskConsitency(ontologiesMap, reasonerFactoryMap, outputFilePath, runs);
		}
		
		//------------------------------------------------------
		// Task Classification
		//------------------------------------------------------

		if(taskNameList.contains("classification"))
		{
			//logger.info("");
			logger.info("--------------------------------------------------");
			//logger.info("");
			taskClassification(ontologiesMap, reasonerFactoryMap, outputFilePath, runs);
		}
		
		//------------------------------------------------------
		// Task Realization
		//------------------------------------------------------
		

		if(taskNameList.contains("realization"))
		{
			//logger.info("");
			logger.info("--------------------------------------------------");
			//logger.info("");
			taskRealization(ontologiesMap, reasonerFactoryMap, outputFilePath, runs);
		}
		
		
	}

	public static void taskLoadReasoner(Map<String, String> ontologiesMap,
			Map<String, OWLReasonerFactory> reasonerFactoryMap, String outputDir, int runs) {
		//logger.info("Task Load Ontology");

		///double evaluationTime = 0;

		for (String reasonerName : reasonerFactoryMap.keySet()) {
			
			logger = LogManager.getLogger(reasonerName);

			//Map<String, ArrayList<Double>> ontoEvalMap = new LinkedHashMap<>();

			//logger.info("");
			//logger.info("--------------------------------------------------");
			//logger.info("");

			logger.info("Evaluation loading " + reasonerName);

			for (String source : ontologiesMap.keySet()) {

				ArrayList<Double> evalResults = new ArrayList<Double>();
				ArrayList<String> evalResultsString = new ArrayList<String>();

				String filename = ontologiesMap.get(source);
				logger.info("");
				logger.info("Ontology: " + source);
				//evaluationTime = 0;


				
				for (int i = 1; i <= runs; i++) {
					
					OWLOntology ontology = loadOntologyFromFile(filename);
					
					
					try {
						double thisTimeRunResult = performLoadingReasoner(ontology, reasonerFactoryMap.get(reasonerName), reasonerName);
						evalResults.add(thisTimeRunResult);
						evalResultsString.add(String.valueOf(thisTimeRunResult));
						//evaluationTime += thisTimeRunResult;
					} catch (Exception | Error e) {
						logger.info(reasonerName + " running error. Ontology:" + source);
						logger.info(e.toString());
						evalResultsString.add(e.toString());
						break; 
					}

					// Calling GC
					System.gc();
				}
				
				if(evalResults.size() == runs)
				{
					Mean mean = new Mean();
					Median median = new Median();
					double[] values = evalResults.stream().mapToDouble(Double::doubleValue).toArray();
					evalResultsString.add(String.valueOf(mean.evaluate(values)));
					evalResultsString.add(String.valueOf(median.evaluate(values)));
					
					logger.info(reasonerName + " Everage Load time on: " + source + "is: " + evalResultsString.get(runs));
					logger.info(reasonerName + " Median Load time on: " + source + "is: " + evalResultsString.get(runs+1));
				}
				writeStringListToCSV(outputDir+"/" + reasonerName + "_Loading.csv", evalResultsString, source);
				//ontoEvalMap.put(source, evalResults);

				//logger.info(reasonerName + " Everage Load time on: " + source + "is: " + evaluationTime / (double) runs);
			}
			//writeMapToCSV(outputDir+"/" + reasonerName + "_Load.csv", ontoEvalMap);
		}
		
		logger =  LogManager.getRootLogger();
	}
	
	public static void taskClassification(Map<String, String> ontologiesMap, Map<String, OWLReasonerFactory> reasonerFactoryMap, String outputDir, int runs) {
		//logger.info("Task Classification Ontology");

		//double evaluationTime = 0;

		for (String reasonerName : reasonerFactoryMap.keySet()) {
			
			logger = LogManager.getLogger(reasonerName);
			

			//Map<String, ArrayList<Double>> ontoEvalMap = new LinkedHashMap<>();

			//logger.info("");
			//logger.info("--------------------------------------------------");
			//logger.info("");

			logger.info("Evaluation classification " + reasonerName);

			for (String source : ontologiesMap.keySet()) {

				ArrayList<Double> evalResults = new ArrayList<Double>();
				ArrayList<String> evalResultsString = new ArrayList<String>();

				String filename = ontologiesMap.get(source);
				logger.info("");
				logger.info("Ontology: " + source);
				//evaluationTime = 0;

				
				
				for (int i = 1; i <= runs; i++) {
					
					OWLOntology ontology = loadOntologyFromFile(filename);

					
					
					try {

						double thisTimeRunResult = 0;
						
						//if(i==1)
						//{
						//	thisTimeRunResult = performClassification(ontology, reasonerFactoryMap.get(reasonerName), reasonerName, outputDir + "/" + reasonerName + "/Classification_" + source);
						//}
						//else 
						
						thisTimeRunResult = performClassification(ontology, reasonerFactoryMap.get(reasonerName), reasonerName, null);
						
						evalResults.add(thisTimeRunResult);
						evalResultsString.add(String.valueOf(thisTimeRunResult));
						//evaluationTime += thisTimeRunResult;
					} catch (Exception | Error e) {
						logger.info(reasonerName + " running error. Ontology:" + source);
						logger.info(e.toString());
						evalResultsString.add(e.toString());
						break; 
					}

					// Calling GC
					System.gc();
				}
				if(evalResults.size() == runs)
				{
					Mean mean = new Mean();
					Median median = new Median();
					double[] values = evalResults.stream().mapToDouble(Double::doubleValue).toArray();
					evalResultsString.add(String.valueOf(mean.evaluate(values)));
					evalResultsString.add(String.valueOf(median.evaluate(values)));
					
					logger.info(reasonerName + " Everage Classification time on: " + source + "is: " + evalResultsString.get(runs));
					logger.info(reasonerName + " Median Classification time on: " + source + "is: " + evalResultsString.get(runs+1));
					
				}
				writeStringListToCSV(outputDir+"/" + reasonerName + "_Classification.csv", evalResultsString, source);
				//ontoEvalMap.put(source, evalResults);

				//logger.info(reasonerName + " Everage Classification time on: " + source + "is: " + evaluationTime / (double) runs);
			}
			//writeMapToCSV(outputDir+"/" + reasonerName + "_Classification.csv", ontoEvalMap);
		}
		
		logger =  LogManager.getRootLogger();
		
	}
	
	public static void taskConsitency(Map<String, String> ontologiesMap, Map<String, OWLReasonerFactory> reasonerFactoryMap, String outputDir, int runs) {
		//logger.info("Task consistency Validation Ontology");

		//double evaluationTime = 0;

		for (String reasonerName : reasonerFactoryMap.keySet()) {
			
			logger = LogManager.getLogger(reasonerName);
			

			//Map<String, ArrayList<Double>> ontoEvalMap = new LinkedHashMap<>();

			//logger.info("");
			//logger.info("--------------------------------------------------");
			//logger.info("");

			logger.info("Evaluation consistency " + reasonerName);

			for (String source : ontologiesMap.keySet()) {

				List<Double> loadingTime = new ArrayList<Double>();
				ArrayList<Double> loadingEvalResults = new ArrayList<Double>();
				ArrayList<String> loadingEvalResultsString = new ArrayList<String>();
				
				ArrayList<Double> evalResults = new ArrayList<Double>();
				ArrayList<String> evalResultsString = new ArrayList<String>();

				String filename = ontologiesMap.get(source);
				logger.info("");
				logger.info("Ontology: " + source);
				//evaluationTime = 0;


				
				for (int i = 1; i <= runs; i++) {
					
					OWLOntology ontology = loadOntologyFromFile(filename);

					
					
					try {
						double thisTimeRunResult = 0;
						loadingTime = new ArrayList<Double>();
						if(i == 1)
							thisTimeRunResult = performConsistencyEvaluation(ontology, reasonerFactoryMap.get(reasonerName), reasonerName, source, outputDir+"/" + reasonerName + "_ConsistencyResult.csv", loadingTime);
						else
							thisTimeRunResult = performConsistencyEvaluation(ontology, reasonerFactoryMap.get(reasonerName), reasonerName, source, null, loadingTime);
						
						evalResults.add(thisTimeRunResult);
						evalResultsString.add(String.valueOf(thisTimeRunResult));
						
						loadingEvalResults.add(loadingTime.get(0));
						loadingEvalResultsString.add(String.valueOf(loadingTime.get(0)));
						
						//evaluationTime += thisTimeRunResult;
					} catch (Exception | Error e) {
						logger.info(reasonerName + " running error. Ontology:" + source);
						logger.info(e.toString());
						evalResultsString.add(e.toString());
						
						if(loadingTime.isEmpty())
						{
							loadingEvalResultsString.add(e.toString());
							break; 
						}
						else
						{
							
							loadingEvalResultsString.clear();
							break;
						}
					}

					// Calling GC
					System.gc();
				}
				
				if(loadingEvalResults.size() == runs)
				{
					Mean mean = new Mean();
					Median median = new Median();
					double[] values = loadingEvalResults.stream().mapToDouble(Double::doubleValue).toArray();
					loadingEvalResultsString.add(String.valueOf(mean.evaluate(values)));
					loadingEvalResultsString.add(String.valueOf(median.evaluate(values)));
					
					logger.info(reasonerName + " Everage Load time on: " + source + "is: " + loadingEvalResultsString.get(runs));
					logger.info(reasonerName + " Median Load time on: " + source + "is: " + loadingEvalResultsString.get(runs+1));
				}
				
				if(loadingEvalResultsString.isEmpty())
				{
					String outLoadingFile = outputDir + "/" + reasonerName +  "_LoadingTimeout.csv";
					writeStringListToCSV(outLoadingFile, new ArrayList<String>(), source);
				}
				else
				{
					writeStringListToCSV(outputDir+"/" + reasonerName + "_Loading.csv", loadingEvalResultsString, source);
				}
				
				
				
				
				if(evalResults.size() == runs)
				{
					Mean mean = new Mean();
					Median median = new Median();
					double[] values = evalResults.stream().mapToDouble(Double::doubleValue).toArray();
					evalResultsString.add(String.valueOf(mean.evaluate(values)));
					evalResultsString.add(String.valueOf(median.evaluate(values)));
					
					logger.info(reasonerName + " Everage Consitency time on: " + source + "is: " + evalResultsString.get(runs));
					logger.info(reasonerName + " Median Consitency time on: " + source + "is: " + evalResultsString.get(runs+1));
					
				}
				writeStringListToCSV(outputDir+"/" + reasonerName + "_Consistency.csv", evalResultsString, source);
				//ontoEvalMap.put(source, evalResults);

				//logger.info(reasonerName + " Everage Consitency Validation time on: " + source + "is: " + evaluationTime / (double) runs);
			}
			//writeMapToCSV(outputDir+"/" + reasonerName + "_Consitency.csv", ontoEvalMap);
		}
		
		
		logger =  LogManager.getRootLogger();
	}
	
	public static void taskRealization(Map<String, String> ontologiesMap, Map<String, OWLReasonerFactory> reasonerFactoryMap, String outputDir, int runs) {
		//logger.info("Task Realization Ontology");

		//double evaluationTime = 0;

		for (String reasonerName : reasonerFactoryMap.keySet()) {
			
			logger = LogManager.getLogger(reasonerName);

			//Map<String, ArrayList<Double>> ontoEvalMap = new LinkedHashMap<>();

			//logger.info("");
			//logger.info("--------------------------------------------------");
			//logger.info("");

			logger.info("Evaluation realization " + reasonerName);

			for (String source : ontologiesMap.keySet()) {

				ArrayList<Double> evalResults = new ArrayList<Double>();
				ArrayList<String> evalResultsString = new ArrayList<String>();

				String filename = ontologiesMap.get(source);
				logger.info("");
				logger.info("Ontology: " + source);
				//evaluationTime = 0;


				
				for (int i = 1; i <= runs; i++) {
					
					OWLOntology ontology = loadOntologyFromFile(filename);

					
					
					try {
						
						double thisTimeRunResult = 0;
						
						//if(i==1)
						//{
						//	thisTimeRunResult = performRealization(ontology, reasonerFactoryMap.get(reasonerName), reasonerName, outputDir + "/" + reasonerName + "/Realization_" + source);
						//}
						//else 
						
						thisTimeRunResult = performRealization(ontology, reasonerFactoryMap.get(reasonerName), reasonerName, null);
						
						
						evalResults.add(thisTimeRunResult);
						evalResultsString.add(String.valueOf(thisTimeRunResult));
						//evaluationTime += thisTimeRunResult;
					} catch (Exception | Error e) {
						logger.info(reasonerName + " running error. Ontology:" + source);
						logger.info(e.toString());
						evalResultsString.add(e.toString());
						break; 
					}

					// Calling GC
					System.gc();
				}
				
				if(evalResults.size() == runs)
				{
					Mean mean = new Mean();
					Median median = new Median();
					double[] values = evalResults.stream().mapToDouble(Double::doubleValue).toArray();
					evalResultsString.add(String.valueOf(mean.evaluate(values)));
					evalResultsString.add(String.valueOf(median.evaluate(values)));
					
					logger.info(reasonerName + " Everage Realization time on: " + source + "is: " + evalResultsString.get(runs));
					logger.info(reasonerName + " Median Realization time on: " + source + "is: " + evalResultsString.get(runs+1));
					
				}
				writeStringListToCSV(outputDir+"/" + reasonerName + "_Realization.csv", evalResultsString, source);
				//ontoEvalMap.put(source, evalResults);

				//logger.info(reasonerName + " Everage Realization time on: " + source + "is: " + evaluationTime / (double) runs);
			}
			//writeMapToCSV(outputDir+"/" + reasonerName + "_Realization.csv", ontoEvalMap);
		}
		
		logger =  LogManager.getRootLogger();
	}
	
	private static void writeMapToCSV(String name, Map<String, ArrayList<Double>>  map)
	{
		try {
			FileWriter writer = new FileWriter(name, false);
			for(String key : map.keySet())
			{
				writer.append(key + ",");
				ArrayList<Double> results = map.get(key);
				writer.append(Stream.of(results.toArray()).map(String::valueOf).collect(Collectors.joining(",")));
				writer.append("\n");
			}
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void writeStringListToCSV(String name, ArrayList<String>  list, String listname)
	{
		try {
			FileWriter writer = new FileWriter(name, true);
			writer.append(listname);
			
			if(!list.isEmpty())
				writer.append(",");
			
			
			ArrayList<String> results = list;
			writer.append(Stream.of(results.toArray()).map(String::valueOf).map(x -> x.replaceAll("[\\t\\n\\r,]+"," ")).collect(Collectors.joining(",")));
			writer.append("\n");
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void writeListToCSV(String name, ArrayList<Double>  list, String listname)
	{
		try {
			FileWriter writer = new FileWriter(name, true);
			writer.append(listname + ",");
			ArrayList<Double> results = list;
			writer.append(Stream.of(results.toArray()).map(String::valueOf).collect(Collectors.joining(",")));
			writer.append("\n");
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void writeErrorToCSV(String name, String error, String listname)
	{
		try {
			FileWriter writer = new FileWriter(name, true);
			writer.append(listname + "," + error);
			writer.append("\n");
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void writeInfencesToFile(String filename, OWLReasoner reasoner){
		
		long startTime, endTime;
		try {
			File file = new File(filename);
			
			if(file.getParentFile() != null || !file.getParentFile().exists())
				file.getParentFile().mkdirs();
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
			OWLOntology exportedOntology = manager.createOntology();
			InferredOntologyGenerator generator = new InferredOntologyGenerator( reasoner );
			generator.fillOntology( manager.getOWLDataFactory(), exportedOntology );
			
			logger.info("Writing inferences to file: " + filename);
			startTime = System.currentTimeMillis();
			manager.saveOntology(exportedOntology, new  ManchesterSyntaxDocumentFormat(), IRI.create(file.toURI()));
			endTime = System.currentTimeMillis();
			logger.info("Writing takes: " + (endTime-startTime)/1000.0 + "s");
		} catch (Exception e) {
			logger.info("Cannot write file: " + filename);
			logger.info(e.toString());
		}
		
	}

	
	public static double performLoadingReasoner(OWLOntology ontology, OWLReasonerFactory reasonerFactory, String name)
			throws Exception {
		long startTime, endTime;
		OWLReasoner reasoner;

	
		logger.info("Starting Loading Reasoner");
			startTime = System.currentTimeMillis();
			reasoner = reasonerFactory.createReasoner(ontology, reasonerConfiguration);
			endTime = System.currentTimeMillis();
		
		reasoner.dispose();

		logger.info("Reasoner Loading takes " + (endTime - startTime)/1000.0 + " s.");

		return (endTime - startTime)/1000.0;
	}

	public static double performConsistencyEvaluation(OWLOntology ontology, OWLReasonerFactory reasonerFactory, String name, String ontologyName, String outputFileName, List<Double> loadingTime) throws Exception {
		long startTime, endTime;
		OWLReasoner reasoner;


		boolean consistent = false;
		
		logger.info("Starting Loading Reasoner");
		startTime = System.currentTimeMillis();
		reasoner = reasonerFactory.createReasoner(ontology, reasonerConfiguration);
		endTime = System.currentTimeMillis();
		loadingTime.add((endTime - startTime)/1000.0);
		
		logger.info("Loading Time: " + (endTime - startTime)/1000.0 + " s.");
		

		TimerTask task = new TimerTask() {
	        public void run() {
	           logger.info("Timeout: Stopping reasoner");
	           reasoner.interrupt();
	        }
	    };
	    Timer timer = new Timer("Timer");
	    timer.schedule(task, reasonerConfiguration.getTimeOut());
	    
	    
	    
		
	    try
	    {
	    	logger.info("Starting Evaluation");
	    	startTime = System.currentTimeMillis();
			consistent = reasoner.isConsistent();
			endTime = System.currentTimeMillis();
			
			logger.info(
					"Reasoner consistency validation takes " + (endTime - startTime)/1000.0 + " s. IsConsistent = " + consistent);
			
	    }
	    finally
	    {
	    	
	    	if(outputFileName != null)
			{
				ArrayList<String> results = new ArrayList<String>();
				results.add(String.valueOf(consistent));
				writeStringListToCSV(outputFileName, results, ontologyName);
			}
	    	
	    	timer.cancel();
	    	reasoner.dispose();
	    }

		

		return (endTime - startTime)/1000.0;
	}

	public static double performClassification(OWLOntology ontology, OWLReasonerFactory reasonerFactory, String name, String outputFileName) throws Exception {
		long startTime, endTime;
		OWLReasoner reasoner;


		logger.info("Starting Loading Reasoner");
		reasoner = reasonerFactory.createReasoner(ontology, reasonerConfiguration);
		
		TimerTask task = new TimerTask() {
	        public void run() {
	           logger.info("Timeout: Stopping reasoner");
	           reasoner.interrupt();
	        }
	    };
	    Timer timer = new Timer("Timer");
	    timer.schedule(task, reasonerConfiguration.getTimeOut());
	    
	    
		
	    try
	    {
	    	logger.info("Starting Evaluation");
	    	startTime = System.currentTimeMillis();
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.DATA_PROPERTY_HIERARCHY, InferenceType.OBJECT_PROPERTY_HIERARCHY);
			endTime = System.currentTimeMillis();
			
			logger.info("Reasoner classification takes " + (endTime - startTime)/1000.0 + " s.");
			if(outputFileName != null)
				writeInfencesToFile(outputFileName, reasoner);
	    }
	    finally
	    {
	    	timer.cancel();
	    	reasoner.dispose();
	    }
		
		
		
		
		
		return (endTime - startTime)/1000.0;
	}

	public static double performRealization(OWLOntology ontology, OWLReasonerFactory reasonerFactory, String name, String outputFileName)
			throws Exception {
		long startTime, endTime;
		OWLReasoner reasoner;


		logger.info("Starting Loading Reasoner");
		reasoner = reasonerFactory.createReasoner(ontology, reasonerConfiguration);
		
		TimerTask task = new TimerTask() {
	        public void run() {
	           logger.info("Timeout: Stopping reasoner");
	           reasoner.interrupt();
	        }
	    };
	    Timer timer = new Timer("Timer");
	    timer.schedule(task, reasonerConfiguration.getTimeOut());
	    
	    
		try
		{
			logger.info("Starting Evaluation");
			startTime = System.currentTimeMillis();
			reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS, InferenceType.DATA_PROPERTY_ASSERTIONS, InferenceType.OBJECT_PROPERTY_ASSERTIONS);
			endTime = System.currentTimeMillis();
			
			logger.info("Reasoner realization takes " + (endTime - startTime)/1000.0 + " s.");
			
			if(outputFileName != null)
				writeInfencesToFile(outputFileName, reasoner);
		}
		finally
		{
			timer.cancel();
			reasoner.dispose();
		}
		
		
		
		
		
		return (endTime - startTime)/1000.0;

	}

	public static OWLOntology loadOntologyFromFile(String filename) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		manager.getOntologyLoaderConfiguration().setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);

		OWLOntology ontology = null;

		try {

			ontology = manager.loadOntologyFromOntologyDocument(new File(filename));

		} catch (OWLOntologyCreationException e) {

			e.printStackTrace();
		}
		return ontology;
	}

}
