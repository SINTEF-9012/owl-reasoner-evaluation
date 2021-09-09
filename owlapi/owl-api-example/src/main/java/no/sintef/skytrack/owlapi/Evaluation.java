package no.sintef.skytrack.owlapi;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import openllet.owlapi.OpenlletReasonerFactory;

public class Evaluation {

	static Logger logger = LoggerFactory.getLogger(Evaluation.class);

	public static void main(String[] args) {
		
		
		Map<String, String> ontologiesMap = new LinkedHashMap<String, String>();
		ontologiesMap.put("http://vicodi.org/ontology", "../../ontologies/vicodi_all.owl");
		ontologiesMap.put("http://www.ifomis.org/acgt/1.0", "../../ontologies/ACGT.owl");
		ontologiesMap.put("http://www.co-ode.org/ontologies/galen", "../../ontologies/full-galen.owl");
		
		
		Map<String, OWLReasonerFactory> reasonerFactoryMap = new LinkedHashMap<>();
		reasonerFactoryMap.put("Pellet",  OpenlletReasonerFactory.getInstance());
		reasonerFactoryMap.put("HermiT", new org.semanticweb.HermiT.ReasonerFactory());
		reasonerFactoryMap.put("JFact", new uk.ac.manchester.cs.jfact.JFactFactory());
		
		//reasonerFactoryMap.put("Snorocket ",  new au.csiro.snorocket.owlapi.SnorocketReasonerFactory() );
		
		
		int RUN = 5;
		
		long evaluationTime = 0;
		
		/*
		 * for(String source : ontologiesMap.keySet()) {
		 * 
		 * String filename = ontologiesMap.get(source); logger.info("");
		 * logger.info("Ontology: " + source); evaluationTime = 0; for(int i = 1; i <=
		 * RUN; i++) { evaluationTime += loadOntologyEvaluation(source, filename); }
		 * 
		 * logger.info("Everage Loading Time of " + filename + " :" +
		 * evaluationTime/(double)RUN);
		 * 
		 * }
		 */
		
		
		logger.info("");
		
		for(String source : ontologiesMap.keySet())
		{
			
			String filename = ontologiesMap.get(source);
			logger.info("Evaluating Reasoner");
			logger.info("Ontology: " + source);
			
			
			for(String reasonerName : reasonerFactoryMap.keySet())
			{
				logger.info("");
				logger.info("Evaluation reasoner " + reasonerName);
				evaluationTime = 0;
				
				for(int i = 1; i <= RUN; i++)
				{
					OWLOntology ontology = loadOntology(source, filename);
					OWLReasoner reasoner = reasonerFactoryMap.get(reasonerName).createReasoner(ontology);
					evaluationTime += performEvaluation(ontology, reasoner).get(0);
				}
				
				logger.info(reasonerName + " Everage Evaluation Time: " + evaluationTime/(double)RUN);
			}
			
		}
	}
	
	
	public static OWLOntology loadOntology(String source, String filename)
	{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IRI iri = IRI.create(source);

		if (filename != null) {
			iri = IRI.create(new File(filename));
		}

		OWLOntology ontology = null;

		try {

			//logger.info("Loading the ontology " + source);
			long startTime = System.currentTimeMillis();
			ontology = manager.loadOntology(iri);
			long endTime = System.currentTimeMillis();

			//logger.info("Loading takes " + (endTime - startTime) + " ms");

		} catch (OWLOntologyCreationException e) {
			
			e.printStackTrace();
		}
		
		int numClassses = ontology.getClassesInSignature(Imports.INCLUDED).size();
		//System.out.println("Number of Classes " + numClassses);
		
		return ontology;
	}
	
	public static long loadOntologyEvaluation(String source, String filename)
	{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IRI iri = IRI.create(source);

		if (filename != null) {
			iri = IRI.create(new File(filename));
		}
		
		long startTime = 0, endTime = 0;

		OWLOntology ontology = null;

		try {

			//logger.info("Loading the ontology " + source);
			startTime = System.currentTimeMillis();
			ontology = manager.loadOntology(iri);
			endTime = System.currentTimeMillis();

			//logger.info("Loading takes " + (endTime - startTime) + " ms");

		} catch (OWLOntologyCreationException e) {
			
			e.printStackTrace();
		}
		
		int numClassses = ontology.getClassesInSignature(Imports.INCLUDED).size();
		//System.out.println("Number of Classes " + numClassses);
		
		return (endTime - startTime);
	}

	public static ArrayList<Long> performEvaluation(OWLOntology ontology, OWLReasoner reasoner) {

		ArrayList<Long> ret = new ArrayList<>();
		
			

			long startTime = System.currentTimeMillis();
			boolean consistent = reasoner.isConsistent();
			long endTime = System.currentTimeMillis();

			logger.info("Validation takes " + (endTime - startTime) + " ms");
			
			ret.add(endTime - startTime);

			if (!consistent) {
				//logger.info("The Model is NOT consitent");
			} else {
				//logger.info("The Model is consitent");
			}
			
			
			
			return ret;

		
	}

}
