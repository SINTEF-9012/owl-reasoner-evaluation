package no.sintef.skytrack.owlapi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class Scheduler {

	static Logger logger =  LogManager.getRootLogger();
	static String path1 = "../../owl-api-example/target/owl-api-example-0.0.1-SNAPSHOT-jar-with-dependencies.jar";
	static String path2 = "../../owl-api-4-example/target/owl-api-4-example-0.0.1-SNAPSHOT-jar-with-dependencies.jar";
	static String FactppLD = "../../owl-api-4-example/lib/FaCT++-linux-v1.6.5/64bit";
	
	
	public static void main(String[] args) {
	
		Map<String, String> ontologiesMap = new LinkedHashMap<String, String>();

		Options options = new Options();

		Option input = new Option("i", "input", true, "input ontologies folder");
		input.setRequired(false);
		options.addOption(input);
		
		Option list = new Option("l", "list", true, "a txt file that lists only names of the ontologies in the input folder to be evaluated");
		list.setRequired(false);
		list.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(list);
		

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

		Option printOnt = new Option("p", "print", false, "print statistics of ontologies");
		printOnt.setRequired(false);
		options.addOption(printOnt);
		
		
		Option fileOnt = new Option("f", "file", true, "list of the ontologies to be evaluated");
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
			// List file
			//------------------------------------------------------
			if(cmd.hasOption("list"))
			{
				String[] listFileNames = cmd.getOptionValues("list");
				Set<String> names = new TreeSet<String>();
				
				for(String listFileName : listFileNames )
				{
					File listFile = new File(listFileName);
					if(listFile.exists())
					{
						try {
							List<String> lines =  Files.readAllLines(listFile.toPath());
							names.addAll(lines);
							
						} catch (IOException e) {
							logger.error("Error when reading file " + listFileName + " : " + e.toString());
							continue;
						}
					}
					else
					{
						logger.error("List File: " + listFileName + " does not exist");
						continue;
					}
				}
				
				names = names.stream().map(String::trim).collect(Collectors.toSet());
				Map<String, String> tMap = new LinkedHashMap<String, String>();
				for(String name : ontologiesMap.keySet())
				{
					if(names.contains(name))
					{
						String path = ontologiesMap.get(name);
						tMap.put(name, path);
						names.remove(name);
					}
				}
				
				for(String name : names)
					logger.info("Ontology: " + name + " not found in input folder");
				
				ontologiesMap.clear();
				ontologiesMap = tMap;
				
				logger.info("There are total " + ontologiesMap.size() + " ontologies in the list files.");
				
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
		// Printing Statistics of ontologies and exit
		//------------------------------------------------------

		
		
		//List<String> cmds = new ArrayList<String>();
		//cmds.add("java");
		//cmds.add("-jar");
		//cmds.add("path");
		//cmds.addAll(Arrays.asList(args));
		
		
		if (cmd.hasOption("print")) {
			
			String processArg = String.join(" ", args);
			
			String command = "java -jar " + path1 + " " + processArg;
			Process process = null;
			try {
				//ProcessBuilder pb = new ProcessBuilder("java", "-jar", path1, args).inheritIO();
				process = Runtime.getRuntime().exec(command);
				BufferedReader processOutput = getOutput(process);
				BufferedReader processError = getError(process);
				String ligne = "";

				while ((ligne = processOutput.readLine()) != null) {
				    logger.info(ligne);
				    
				}
				
				while ((ligne = processError.readLine()) != null) {
				    logger.error(ligne);
				    
				}
				process.waitFor();
		        
				
			} catch (Exception e) {
				
				logger.error("Error: " + e.toString());
			}
			finally
			{
				process.destroy();
			}
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
				Arrays.asList("HermiT", "JFact", "Openllet", "Konclude", "Factpp", "Pellet"));
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
		
		logger.info("");
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
		logger.info("");
		
		
		evaluate(ontologiesMap, reasonersName, tasksName, runs, outputFilePath);
		
		
	}

	public static void evaluate(Map<String, String> ontologiesMap, String[] reasoners,  String[] tasks, int runs, String outputPath) {
		
		long reasonerTimeOut = 2*60*60*1000;

		for(String task : tasks)
		{
			String processArg = "-t " + task + " -n " + runs + " -o " + outputPath;
			for (String reasonerName : reasoners) 
			{
				String reasonerArg = processArg + " -r " + reasonerName;
				
				String command = "java -jar ";
				
				if(reasonerName.equals(("Factpp")))
					command = "java -Djava.library.path=" + FactppLD + " -jar ";
				
				
				
				if(reasonerName.equals("Factpp") ||  reasonerName.equals("Pellet")) 
					command  = command + path2;
				else 
					command = command + path1;
				
				
				
				
				for (String source : ontologiesMap.values()) 
				{
					String ontologyArg = reasonerArg + " -f " + source;
					String finalCommand = command + " " + ontologyArg;
					
					Timer timer = new Timer("Timer");
					
					Process process = null;
					try {
					
						//logger.info(finalCommand);
						
						process = Runtime.getRuntime().exec(finalCommand);
						BufferedReader processOutput = getOutput(process);
						BufferedReader processError = getError(process);
						
						
						final Process timerProcess = process;
						TimerTask timerTask = new TimerTask() {
					        public void run() {
					           logger.info("Timeout: Stopping process");
					           if(timerProcess != null && timerProcess.isAlive())
					        	   timerProcess.destroyForcibly();
					        }
					        
					    };
					    
					    timer.schedule(timerTask, reasonerTimeOut);
					    
					    
						String ligne = "";

						while ((ligne = processOutput.readLine()) != null) {
						    logger.info(ligne);
						   
						    if(ligne.contains("takes"))
						    {
						    	
					    		timer.cancel();
					    		timer = new Timer("Timer");
					    		timerTask = new TimerTask() {
						        public void run() {
						           logger.info("Timeout: Stopping process");
						           if(timerProcess != null && timerProcess.isAlive())
						        	   timerProcess.destroyForcibly();
						        }
							        
							    };
							    
							    timer.schedule(timerTask, reasonerTimeOut);
						    }
						}
						
						while ((ligne = processError.readLine()) != null) {
						    logger.error(ligne);
						}
						process.waitFor();
						
					} catch (Exception e) {
						
						logger.error("Error: " + e.toString());
					}
					finally
					{
						timer.cancel();
						 if(process != null)
							 process.destroyForcibly();
					}
				}

			}
		}
		

	}
		
	
	private static BufferedReader getOutput(Process p) {
	    return new BufferedReader(new InputStreamReader(p.getInputStream()));
	}
	
	private static BufferedReader getError(Process p) {
	    return new BufferedReader(new InputStreamReader(p.getErrorStream()));
	}


	}


