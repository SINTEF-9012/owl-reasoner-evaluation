package no.sintef.skytrack.owlapi;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.owllink.OWLlinkHTTPXMLReasonerFactory;
import org.semanticweb.owlapi.owllink.OWLlinkReasonerConfigurationImpl;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.cli.ParseException;

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

		Option reasoners = new Option("r", "reasoner", true,
				"list of reasoner to evaluate (HermiT, JFact, Pellet, KonClude)");
		reasoners.setRequired(false);
		reasoners.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(reasoners);

		Option tasks = new Option("t", "task", true,
				"list of reasoner tasks to evaluate (loading, consistency, classification, realization)");
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

		String inputFilePath = cmd.getOptionValue("input");
		if (inputFilePath == null)
			inputFilePath = "../../../ontologies";
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

		if (cmd.hasOption("print")) {
			printOntologyStatistics(ontologiesMap);
			System.exit(0);
		}

		String outputFilePath = cmd.getOptionValue("output");
		if (outputFilePath == null)
			outputFilePath = "./output";
		File outputDir = new File(outputFilePath);
		if (!outputDir.exists())
			outputDir.mkdir();

		ArrayList<String> supportReasoners = new ArrayList<String>(
				Arrays.asList("HermiT", "JFact", "Pellet", "Konclude"));
		String[] reasonersName = cmd.getOptionValues("reasoners");
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

		ArrayList<String> supportTasks = new ArrayList<String>(
				Arrays.asList("loading", "consistency", "classification", "realization"));

		String[] tasksName = cmd.getOptionValues("reasoners");
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

		Integer runs = null;

		try {
			runs = (Integer) cmd.getParsedOptionValue("iterations");
		} catch (ParseException e1) {

		}

		if (runs == null)
			runs = 10;

		logger.info("inputFilePath: " + inputFilePath);
		logger.info("outputFilePath: " + outputFilePath);
		logger.info("iterations: " + runs);
		logger.info("reasoner: " + Arrays.toString(reasonersName));
		logger.info("task: " + Arrays.toString(tasksName));

		logger.info("");
		logger.info("--------------------------------------------------");
		logger.info("");

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

		
		ArrayList<String> taskNameList = new ArrayList<String>(Arrays.asList(tasksName));
		
		if(taskNameList.contains("loading"))
		{
			logger.info("");
			logger.info("--------------------------------------------------");
			logger.info("");
			taskLoadOntology(ontologiesMap, reasonerFactoryMap, outputFilePath, runs);
		}
	}

	public static void taskLoadOntology(Map<String, String> ontologiesMap,
			Map<String, OWLReasonerFactory> reasonerFactoryMap, String outputDir, int runs) {
		logger.info("Task Load Ontology");

		long evaluationTime = 0;

		for (String reasonerName : reasonerFactoryMap.keySet()) {

			Map<String, ArrayList<Long>> ontoEvalMap = new LinkedHashMap<>();

			logger.info("");
			logger.info("--------------------------------------------------");
			logger.info("");

			logger.info("Evaluation reasoner " + reasonerName);

			for (String source : ontologiesMap.keySet()) {

				ArrayList<Long> evalResults = new ArrayList<Long>(runs);

				String filename = ontologiesMap.get(source);
				logger.info("Ontology: " + source);
				evaluationTime = 0;

				for (int i = 1; i <= runs; i++) {
					OWLOntology ontology = loadOntology(source, filename);

					try {
						long thisTimeRunResult = performLoadingReasoner(ontology, reasonerFactoryMap.get(reasonerName), reasonerName);
						evalResults.add(thisTimeRunResult);
						evaluationTime += thisTimeRunResult;
					} catch (Exception e) {
						logger.info(reasonerName + "running error");
						break;
					}

					// Calling GC
					System.gc();
				}
				
				ontoEvalMap.put(source, evalResults);

				logger.info(reasonerName + " Everage Load validation time on: " + source + "is: " + evaluationTime / (double) runs);
			}
			writeMapToCSV(outputDir+"/" + reasonerName + "_Load.csv", ontoEvalMap);
		}
	}
	
	private static void writeMapToCSV(String name, Map<String, ArrayList<Long>>  map)
	{
		try {
			FileWriter writer = new FileWriter(name, false);
			for(String key : map.keySet())
			{
				writer.append(key + ",");
				ArrayList<Long> results = map.get(key);
				writer.append(Stream.of(results.toArray()).map(String::valueOf).collect(Collectors.joining(",")));
				writer.append("\n");
			}
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
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

	public static long performLoadingReasoner(OWLOntology ontology, OWLReasonerFactory reasonerFactory, String name)
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

		logger.info("Reasoner Loading takes " + (endTime - startTime) + " ms.");

		return (endTime - startTime);
	}

	public static long performConsistencyEvaluation(OWLOntology ontology, OWLReasonerFactory reasonerFactory,
			String name) throws Exception {
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

		boolean consistent = reasoner.isConsistent();
		endTime = System.currentTimeMillis();
		reasoner.dispose();

		logger.info(
				"Reasoner consistency validation takes " + (endTime - startTime) + " ms. IsConsisten = " + consistent);

		return (endTime - startTime);
	}

	public static long performClassification(OWLOntology ontology, OWLReasonerFactory reasonerFactory, String name)
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
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		endTime = System.currentTimeMillis();
		reasoner.dispose();
		logger.info("Reasoner classification takes " + (endTime - startTime) + " ms.");
		return (endTime - startTime);
	}

	public static long performRealization(OWLOntology ontology, OWLReasonerFactory reasonerFactory, String name)
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
		reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
		endTime = System.currentTimeMillis();
		reasoner.dispose();
		logger.info("Reasoner realization takes " + (endTime - startTime) + " ms.");
		return (endTime - startTime);

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

	public static long loadOntologyEvaluation(String source, String filename) {
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

			logger.info("Loading takes " + (endTime - startTime) + " ms");

		} catch (OWLOntologyCreationException e) {

			e.printStackTrace();
		}

		int numClassses = ontology.getClassesInSignature(Imports.INCLUDED).size();
		// System.out.println("Number of Classes " + numClassses);

		return (endTime - startTime);
	}

}
