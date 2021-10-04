package no.sintef.skytrack.owlapi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.owllink.OWLlinkHTTPXMLReasonerFactory;
import org.semanticweb.owlapi.owllink.OWLlinkReasonerConfigurationImpl;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import openllet.owlapi.OpenlletReasonerFactory;

public class Evaluation {

	static Logger logger = LoggerFactory.getLogger(Evaluation.class);

	static OWLReasonerConfiguration koncludeReasonerConfiguration;

	public static void main(String[] args) {

		Map<String, String> ontologiesMap = new LinkedHashMap<String, String>();
		try {
			koncludeReasonerConfiguration = new OWLlinkReasonerConfigurationImpl(new URL("http://localhost:8080"));
		} catch (MalformedURLException e2) {
			e2.printStackTrace();
		}

		Options options = new Options();

		Option input = new Option("i", "input", true, "input ontologies folder");
		input.setRequired(false);
		options.addOption(input);

		Option output = new Option("o", "output", true, "output folder");
		output.setRequired(false);
		options.addOption(output);

		Option reasoners = new Option("r", "reasoner", true, "list of reasoner to evaluate (HermiT, JFact, Pellet, KonClude)");
		reasoners.setRequired(false);
		reasoners.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(reasoners);

		Option tasks = new Option("t", "task", true, "list of reasoner tasks to evaluate (loading, consistency, classification, realization)");
		tasks.setRequired(false);
		tasks.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(tasks);

		Option iterations = new Option("n", "iterations", true, "number of iterations for each evaluation");
		iterations.setRequired(false);
		iterations.setType(Integer.class);
		options.addOption(iterations);

		Option printOnt = new Option("p", "print", false, "print statistics of ontologies");
		printOnt.setRequired(false);
		options.addOption(printOnt);
		
		
		Option fileOnt = new Option("f", "file", true, "print statistics of ontologies");
		fileOnt.setRequired(false);
		fileOnt.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(fileOnt);
		
		Option jump = new Option("j", "jump", true, "jump to current ontology");
		jump.setRequired(false);
		options.addOption(jump);
		
		
		

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;// not a good practice, it serves it purpose

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("Evaluation", options);

			System.exit(1);
		}

		
		//------------------------------------------------------
		// Input Dir
		//------------------------------------------------------
		String inputFilePath = cmd.getOptionValue("input");
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
			File ontoFie = new File(ontoToJump);
			if(ontoFie.exists() && ontoFie.isFile() && ontologiesMap.containsKey(ontoFie.getName()))
			{
				ArrayList<String> keySet = new ArrayList<String>(ontologiesMap.keySet());
				for(String name : keySet)
				{
					if(name.equals(ontoFie.getName()))
						break;
					
					ontologiesMap.remove(name);
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
				if (file.exists() && file.isFile()) {
					ontologiesMap.put(file.getName(), file.getAbsolutePath());
				}
			}
			
		}
		
		//------------------------------------------------------
		// Printing Statistics of ontologies and exit
		//------------------------------------------------------

		if (cmd.hasOption("print")) {
			printOntologyStatistics(ontologiesMap);
			System.exit(0);
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
				Arrays.asList("HermiT", "JFact", "Pellet", "Konclude"));
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

		try {
			runs = (Integer) cmd.getParsedOptionValue("iterations");
		} catch (ParseException e1) {

		}

		if (runs == null)
			runs = 10;
		
		//------------------------------------------------------
		// Printing args
		//------------------------------------------------------
		

		logger.info("inputFilePath: " + inputFilePath);
		logger.info("outputFilePath: " + outputFilePath);
		logger.info("iterations: " + runs);
		logger.info("reasoner: " + Arrays.toString(reasonersName));
		logger.info("task: " + Arrays.toString(tasksName));

		logger.info("");
		logger.info("--------------------------------------------------");
		logger.info("");
		
		
		
		//------------------------------------------------------
		// Create reasoners
		//------------------------------------------------------

		Map<String, OWLReasonerFactory> reasonerFactoryMap = new LinkedHashMap<>();

		ArrayList<String> reasonersNameList = new ArrayList<String>(Arrays.asList(reasonersName));
		if (reasonersNameList.contains("HermiT"))
			reasonerFactoryMap.put("HermiT", new org.semanticweb.HermiT.ReasonerFactory());
		if (reasonersNameList.contains("JFact"))
			reasonerFactoryMap.put("JFact", new uk.ac.manchester.cs.jfact.JFactFactory());
		if (reasonersNameList.contains("Pellet"))
			reasonerFactoryMap.put("Pellet", OpenlletReasonerFactory.getInstance());
		if (reasonersNameList.contains("Konclude"))
			reasonerFactoryMap.put("Konclude", new OWLlinkHTTPXMLReasonerFactory());

		
		
		//------------------------------------------------------
		// Task Load Reasoners
		//------------------------------------------------------
		
		ArrayList<String> taskNameList = new ArrayList<String>(Arrays.asList(tasksName));
		
		if(taskNameList.contains("loading"))
		{
			logger.info("");
			logger.info("--------------------------------------------------");
			logger.info("");
			taskLoadReasoner(ontologiesMap, reasonerFactoryMap, outputFilePath, runs);
		}
		//------------------------------------------------------
		// Task Consistency
		//------------------------------------------------------
		
		if(taskNameList.contains("consistency"))
		{
			logger.info("");
			logger.info("--------------------------------------------------");
			logger.info("");
			taskConsitency(ontologiesMap, reasonerFactoryMap, outputFilePath, runs);
		}
		
		//------------------------------------------------------
		// Task Classification
		//------------------------------------------------------

		if(taskNameList.contains("classification"))
		{
			logger.info("");
			logger.info("--------------------------------------------------");
			logger.info("");
			taskClassification(ontologiesMap, reasonerFactoryMap, outputFilePath, runs);
		}
		
		//------------------------------------------------------
		// Task Realization
		//------------------------------------------------------
		

		if(taskNameList.contains("realization"))
		{
			logger.info("");
			logger.info("--------------------------------------------------");
			logger.info("");
			taskRealization(ontologiesMap, reasonerFactoryMap, outputFilePath, runs);
		}
		
		
	}

	public static void taskLoadReasoner(Map<String, String> ontologiesMap,
			Map<String, OWLReasonerFactory> reasonerFactoryMap, String outputDir, int runs) {
		logger.info("Task Load Ontology");

		double evaluationTime = 0;

		for (String reasonerName : reasonerFactoryMap.keySet()) {

			Map<String, ArrayList<Double>> ontoEvalMap = new LinkedHashMap<>();

			logger.info("");
			logger.info("--------------------------------------------------");
			logger.info("");

			logger.info("Evaluation reasoner " + reasonerName);

			for (String source : ontologiesMap.keySet()) {

				ArrayList<Double> evalResults = new ArrayList<Double>(runs);

				String filename = ontologiesMap.get(source);
				logger.info("");
				logger.info("Ontology: " + source);
				evaluationTime = 0;

				OWLOntology ontology = loadOntologyFromFile(filename);
				if (reasonerName.equals("Konclude")) {
					try {
						ontology.getOWLOntologyManager().createOntology(ontology.importsClosure().flatMap(OWLOntology::logicalAxioms).collect(Collectors.toSet()));
					} catch (OWLOntologyCreationException e) {
						logger.info(reasonerName + " Loading ontology error: " + source);
						logger.info(e.getMessage());
						continue;
					}
				}
				
				for (int i = 1; i <= runs; i++) {
					
					try {
						double thisTimeRunResult = performLoadingReasoner(ontology, reasonerFactoryMap.get(reasonerName), reasonerName);
						evalResults.add(thisTimeRunResult);
						evaluationTime += thisTimeRunResult;
					} catch (Exception e) {
						logger.info(reasonerName + " running error. Ontology:" + source);
						logger.info(e.getMessage());
						break; 
					}

					// Calling GC
					System.gc();
				}
				writeListToCSV(outputDir+"/" + reasonerName + "_Realization.csv", evalResults, source);
				ontoEvalMap.put(source, evalResults);

				logger.info(reasonerName + " Everage Load time on: " + source + "is: " + evaluationTime / (double) runs);
			}
			//writeMapToCSV(outputDir+"/" + reasonerName + "_Load.csv", ontoEvalMap);
		}
	}
	
	public static void taskClassification(Map<String, String> ontologiesMap, Map<String, OWLReasonerFactory> reasonerFactoryMap, String outputDir, int runs) {
		logger.info("Task Classification Ontology");

		double evaluationTime = 0;

		for (String reasonerName : reasonerFactoryMap.keySet()) {

			Map<String, ArrayList<Double>> ontoEvalMap = new LinkedHashMap<>();

			logger.info("");
			logger.info("--------------------------------------------------");
			logger.info("");

			logger.info("Evaluation reasoner " + reasonerName);

			for (String source : ontologiesMap.keySet()) {

				ArrayList<Double> evalResults = new ArrayList<Double>(runs);

				String filename = ontologiesMap.get(source);
				logger.info("");
				logger.info("Ontology: " + source);
				evaluationTime = 0;

				OWLOntology ontology = loadOntologyFromFile(filename);
				if (reasonerName.equals("Konclude")) {
					try {
						ontology = ontology.getOWLOntologyManager().createOntology(ontology.importsClosure().flatMap(OWLOntology::logicalAxioms).collect(Collectors.toSet()));
					} catch (OWLOntologyCreationException e) {
						logger.info(reasonerName + " Loading ontology error: " + source);
						logger.info(e.getMessage());
						continue;
					}
				}
				
				for (int i = 1; i <= runs; i++) {
					
					try {

						double thisTimeRunResult = 0;
						
						if(i==1)
						{
							thisTimeRunResult = performClassification(ontology, reasonerFactoryMap.get(reasonerName), reasonerName, outputDir + "/" + reasonerName + "/Classification_" + source);
						}
						else 
						{
							thisTimeRunResult = performClassification(ontology, reasonerFactoryMap.get(reasonerName), reasonerName, null);
						}
						evalResults.add(thisTimeRunResult);
						evaluationTime += thisTimeRunResult;
					} catch (Exception e) {
						logger.info(reasonerName + " running error. Ontology:" + source);
						logger.info(e.getMessage());
						break; 
					}

					// Calling GC
					System.gc();
				}
				writeListToCSV(outputDir+"/" + reasonerName + "_Classification.csv", evalResults, source);
				ontoEvalMap.put(source, evalResults);

				logger.info(reasonerName + " Everage Classification time on: " + source + "is: " + evaluationTime / (double) runs);
			}
			//writeMapToCSV(outputDir+"/" + reasonerName + "_Classification.csv", ontoEvalMap);
		}
	}
	
	public static void taskConsitency(Map<String, String> ontologiesMap, Map<String, OWLReasonerFactory> reasonerFactoryMap, String outputDir, int runs) {
		logger.info("Task consistency Validation Ontology");

		double evaluationTime = 0;

		for (String reasonerName : reasonerFactoryMap.keySet()) {

			Map<String, ArrayList<Double>> ontoEvalMap = new LinkedHashMap<>();

			logger.info("");
			logger.info("--------------------------------------------------");
			logger.info("");

			logger.info("Evaluation reasoner " + reasonerName);

			for (String source : ontologiesMap.keySet()) {

				ArrayList<Double> evalResults = new ArrayList<Double>(runs);

				String filename = ontologiesMap.get(source);
				logger.info("");
				logger.info("Ontology: " + source);
				evaluationTime = 0;

				OWLOntology ontology = loadOntologyFromFile(filename);
				if (reasonerName.equals("Konclude")) {
					try {
						logger.info("Preparing ontology for Konclude.");
						ontology = ontology.getOWLOntologyManager().createOntology(ontology.importsClosure().flatMap(OWLOntology::logicalAxioms).collect(Collectors.toSet()));
						logger.info("Preparing ontology for Konclude. Done");
					} catch (OWLOntologyCreationException e) {
						logger.info(reasonerName + " Loading ontology error: " + source);
						continue;
					}
				}
				
				for (int i = 1; i <= runs; i++) {
					
					try {
						double thisTimeRunResult = performConsistencyEvaluation(ontology, reasonerFactoryMap.get(reasonerName), reasonerName);
						evalResults.add(thisTimeRunResult);
						evaluationTime += thisTimeRunResult;
					} catch (Exception e) {
						logger.info(reasonerName + " running error. Ontology:" + source);
						logger.info(e.getMessage());
						break; 
					}

					// Calling GC
					System.gc();
				}
				writeListToCSV(outputDir+"/" + reasonerName + "_Consistency.csv", evalResults, source);
				ontoEvalMap.put(source, evalResults);

				logger.info(reasonerName + " Everage Consitency Validation time on: " + source + "is: " + evaluationTime / (double) runs);
			}
			//writeMapToCSV(outputDir+"/" + reasonerName + "_Consitency.csv", ontoEvalMap);
		}
	}
	
	public static void taskRealization(Map<String, String> ontologiesMap, Map<String, OWLReasonerFactory> reasonerFactoryMap, String outputDir, int runs) {
		logger.info("Task Realization Ontology");

		double evaluationTime = 0;

		for (String reasonerName : reasonerFactoryMap.keySet()) {

			Map<String, ArrayList<Double>> ontoEvalMap = new LinkedHashMap<>();

			logger.info("");
			logger.info("--------------------------------------------------");
			logger.info("");

			logger.info("Evaluation reasoner " + reasonerName);

			for (String source : ontologiesMap.keySet()) {

				ArrayList<Double> evalResults = new ArrayList<Double>(runs);

				String filename = ontologiesMap.get(source);
				logger.info("");
				logger.info("Ontology: " + source);
				evaluationTime = 0;

				OWLOntology ontology = loadOntologyFromFile(filename);
				if (reasonerName.equals("Konclude")) {
					try {
						ontology = ontology.getOWLOntologyManager().createOntology(ontology.importsClosure().flatMap(OWLOntology::logicalAxioms).collect(Collectors.toSet()));
					} catch (OWLOntologyCreationException e) {
						logger.info(reasonerName + " Loading ontology error: " + source);
						logger.info(e.getMessage());
						continue;
					}
				}
				
				for (int i = 1; i <= runs; i++) {
					
					try {
						
						double thisTimeRunResult = 0;
						
						if(i==1)
						{
							thisTimeRunResult = performRealization(ontology, reasonerFactoryMap.get(reasonerName), reasonerName, outputDir + "/" + reasonerName + "/Realization_" + source);
						}
						else 
						{
							thisTimeRunResult = performRealization(ontology, reasonerFactoryMap.get(reasonerName), reasonerName, null);
						}
						
						evalResults.add(thisTimeRunResult);
						evaluationTime += thisTimeRunResult;
					} catch (Exception e) {
						logger.info(reasonerName + " running error. Ontology:" + source);
						logger.info(e.getMessage());
						break; 
					}

					// Calling GC
					System.gc();
				}
				
				writeListToCSV(outputDir+"/" + reasonerName + "_Realization.csv", evalResults, source);
				ontoEvalMap.put(source, evalResults);

				logger.info(reasonerName + " Everage Realization time on: " + source + "is: " + evaluationTime / (double) runs);
			}
			//writeMapToCSV(outputDir+"/" + reasonerName + "_Realization.csv", ontoEvalMap);
		}
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
			logger.info(e.getMessage());
		}
		
	}

	public static void printOntologyStatistics(Map<String, String> ontologiesMap) {
		for (String name : ontologiesMap.keySet()) {
			logger.info("");
			logger.info("--------------------------------------------------");
			logger.info("");
			logger.info("Ontology: " + name);
			String path = ontologiesMap.get(name);
			OWLOntology onto = loadOntologyFromFile(path);

			if (onto != null) {

				logger.info("Classes:" + onto.getClassesInSignature(Imports.INCLUDED).size());
				logger.info("Individuals: " + onto.getIndividualsInSignature(Imports.INCLUDED).size());
				logger.info("Axioms: " + onto.getAxiomCount());

			}

		}
	}

	public static double performLoadingReasoner(OWLOntology ontology, OWLReasonerFactory reasonerFactory, String name)
			throws Exception {
		long startTime, endTime;
		OWLReasoner reasoner;

		if (name.equals("Konclude")) {
			ontology.getOWLOntologyManager().createOntology(
					ontology.importsClosure().flatMap(OWLOntology::logicalAxioms).collect(Collectors.toSet()));
			startTime = System.currentTimeMillis();
			reasoner = reasonerFactory.createReasoner(ontology, koncludeReasonerConfiguration);

		} else {
			startTime = System.currentTimeMillis();
			reasoner = reasonerFactory.createReasoner(ontology);
		}
		endTime = System.currentTimeMillis();
		reasoner.dispose();

		logger.info("Reasoner Loading takes " + (endTime - startTime)/1000.0 + " s.");

		return (endTime - startTime)/1000.0;
	}

	public static double performConsistencyEvaluation(OWLOntology ontology, OWLReasonerFactory reasonerFactory, String name) throws Exception {
		long startTime, endTime;
		OWLReasoner reasoner;

		if (name.equals("Konclude")) {
			startTime = System.currentTimeMillis();
			reasoner = reasonerFactory.createReasoner(ontology, koncludeReasonerConfiguration);

		} else {
			startTime = System.currentTimeMillis();
			reasoner = reasonerFactory.createReasoner(ontology);
		}

		boolean consistent = reasoner.isConsistent();
		endTime = System.currentTimeMillis();
		reasoner.dispose();

		logger.info(
				"Reasoner consistency validation takes " + (endTime - startTime)/1000.0 + " s. IsConsisten = " + consistent);

		return (endTime - startTime)/1000.0;
	}

	public static double performClassification(OWLOntology ontology, OWLReasonerFactory reasonerFactory, String name, String outputFileName) throws Exception {
		long startTime, endTime;
		OWLReasoner reasoner;

		if (name.equals("Konclude")) {
			startTime = System.currentTimeMillis();
			reasoner = reasonerFactory.createReasoner(ontology, koncludeReasonerConfiguration);

		} else {
			startTime = System.currentTimeMillis();
			reasoner = reasonerFactory.createReasoner(ontology);
		}
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.DATA_PROPERTY_HIERARCHY, InferenceType.OBJECT_PROPERTY_HIERARCHY);
		
		
		endTime = System.currentTimeMillis();
		
		logger.info("Reasoner classification takes " + (endTime - startTime)/1000.0 + " s.");
		if(outputFileName != null)
			writeInfencesToFile(outputFileName, reasoner);
		
		
		reasoner.dispose();
		
		return (endTime - startTime)/1000.0;
	}

	public static double performRealization(OWLOntology ontology, OWLReasonerFactory reasonerFactory, String name, String outputFileName)
			throws Exception {
		long startTime, endTime;
		OWLReasoner reasoner;

		if (name.equals("Konclude")) {
			startTime = System.currentTimeMillis();
			reasoner = reasonerFactory.createReasoner(ontology, koncludeReasonerConfiguration);

		} else {
			startTime = System.currentTimeMillis();
			reasoner = reasonerFactory.createReasoner(ontology);
		}
		reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS, InferenceType.DATA_PROPERTY_ASSERTIONS, InferenceType.OBJECT_PROPERTY_ASSERTIONS);
		endTime = System.currentTimeMillis();
		
		logger.info("Reasoner realization takes " + (endTime - startTime)/1000.0 + " s.");
		
		if(outputFileName != null)
			writeInfencesToFile(outputFileName, reasoner);
		reasoner.dispose();
		
		
		
		return (endTime - startTime)/1000.0;

	}

	public static OWLOntology loadOntologyFromFile(String filename) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology ontology = null;

		try {

			ontology = manager.loadOntologyFromOntologyDocument(new File(filename));

		} catch (OWLOntologyCreationException e) {

			e.printStackTrace();
		}
		return ontology;
	}

	public static OWLOntology loadOntology(String source, String filename) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IRI iri = IRI.create(source);

		if (filename != null) {
			iri = IRI.create(new File(filename));
		}

		OWLOntology ontology = null;

		try {
			ontology = manager.loadOntology(iri);

		} catch (OWLOntologyCreationException e) {

			e.printStackTrace();
		}
		return ontology;
	}

	public static double loadOntologyEvaluation(String source, String filename) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IRI iri = IRI.create(source);

		if (filename != null) {
			iri = IRI.create(new File(filename));
		}

		long startTime = 0, endTime = 0;

		OWLOntology ontology = null;

		try {

			// logger.info("Loading the ontology " + source);
			startTime = System.currentTimeMillis();
			ontology = manager.loadOntology(iri);
			endTime = System.currentTimeMillis();

			logger.info("Loading takes " + (endTime - startTime)/1000.0 + " s");

		} catch (OWLOntologyCreationException e) {

			e.printStackTrace();
		}

		//int numClassses = ontology.getClassesInSignature(Imports.INCLUDED).size();
		// System.out.println("Number of Classes " + numClassses);

		return (endTime - startTime)/1000.0;
	}

}
