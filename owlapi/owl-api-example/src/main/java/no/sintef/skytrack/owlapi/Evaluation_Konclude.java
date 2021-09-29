package no.sintef.skytrack.owlapi;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.*;

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

public class Evaluation_Konclude {
	
	static Logger logger = LoggerFactory.getLogger(Evaluation_Konclude.class);
	
	static OWLReasonerConfiguration koncludeReasonerConfiguration;

	public static void main(String[] args) throws MalformedURLException, OWLOntologyCreationException {
		
		logger.info("");
		logger.info("--------------------------------------------------");
		logger.info("");
		
		Map<String, String> ontologiesMap = new LinkedHashMap<String, String>();
		ontologiesMap.put("http://vicodi.org/ontology#", "../../../ontologies/vicodi_all.owl");
		ontologiesMap.put("http://www.ifomis.org/acgt/1.0#", "../../../ontologies/ACGT.owl");
		ontologiesMap.put("http://www.co-ode.org/ontologies/galen#", "../../../ontologies/full-galen.owl");
		ontologiesMap.put("http://purl.org/sig/ont/fma.owl#", "../../../ontologies/fma.owl");
		ontologiesMap.put("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#", "../../../ontologies/ncit.owl");
		ontologiesMap.put("http://purl.bioontology.org/ontology/MESH/", "../../../ontologies/MESH.owl");
		//ontologiesMap.put("http://purl.bioontology.org/ontology/MESH/", "../../ontologies/MESH.ttl");
		
		
		Map<String, OWLReasonerFactory> reasonerFactoryMap = new LinkedHashMap<>();
		reasonerFactoryMap.put("Konclude",   new OWLlinkHTTPXMLReasonerFactory());
		
		
		koncludeReasonerConfiguration = new OWLlinkReasonerConfigurationImpl(new URL("http://localhost:8080"));
	 
		
		int RUN = 5;
		
		long evaluationTime = 0;
		long reasonerLoadingTime = 0;
		long startTime, endTime;
		long reasonerClassificationTime = 0;
		long reasonerConsistencyTime = 0;
		
		
		
		/*
		 * logger.info("");
		 * 
		 * for(String source : ontologiesMap.keySet()) {
		 * 
		 * String filename = ontologiesMap.get(source);
		 * logger.info("Evaluating Reasoner"); logger.info("Ontology: " + source);
		 * 
		 * 
		 * for(String reasonerName : reasonerFactoryMap.keySet()) { logger.info("");
		 * logger.info("Evaluation reasoner " + reasonerName); evaluationTime = 0;
		 * 
		 * for(int i = 1; i <= RUN; i++) { OWLOntology ontology = loadOntology(source,
		 * filename);
		 * 
		 * OWLReasoner reasoner = null;
		 * 
		 * if(reasonerName.equals("Konclude")) { //ontology =
		 * ontology.getOWLOntologyManager().createOntology(ontology.importsClosure().
		 * flatMap(OWLOntology::logicalAxioms).collect(Collectors.toSet())); startTime =
		 * System.currentTimeMillis(); try { reasoner =
		 * reasonerFactoryMap.get(reasonerName).createReasoner(ontology,
		 * koncludeReasonerConfiguration); } catch (Exception e){ //e.printStackTrace();
		 * logger.info("Error - Load ontology again"); ontology =
		 * ontology.getOWLOntologyManager().createOntology(ontology.importsClosure().
		 * flatMap(OWLOntology::logicalAxioms).collect(Collectors.toSet()));
		 * 
		 * try { startTime = System.currentTimeMillis(); reasoner =
		 * reasonerFactoryMap.get(reasonerName).createReasoner(ontology,
		 * koncludeReasonerConfiguration); } catch (Exception ex){ ex.printStackTrace();
		 * }
		 * 
		 * 
		 * }
		 * 
		 * endTime = System.currentTimeMillis();
		 * 
		 * logger.info("Reasoner Loading takes " + (endTime - startTime) + " ms");
		 * reasonerLoadingTime += (endTime - startTime);
		 * 
		 * 
		 * } else reasoner =
		 * reasonerFactoryMap.get(reasonerName).createReasoner(ontology);
		 * 
		 * 
		 * if(reasoner != null) evaluationTime += performEvaluation(ontology,
		 * reasoner).get(0);
		 * 
		 * 
		 * 
		 * }
		 * 
		 * logger.info(reasonerName + " Everage Loading Time on: " + source + "is: " +
		 * reasonerLoadingTime / (double) RUN);
		 * 
		 * logger.info(reasonerName + " Everage Validation Time on: " + source + "is: "
		 * + evaluationTime / (double) RUN); }
		 * 
		 * }
		 */
		
		
		logger.info("Evaluating Reasoner consistency validation");

		for (String reasonerName : reasonerFactoryMap.keySet()) {
			logger.info("");
			logger.info("Evaluation reasoner " + reasonerName);

			for (String source : ontologiesMap.keySet()) {

				String filename = ontologiesMap.get(source);
				logger.info("Ontology: " + source);
				reasonerConsistencyTime = 0;

				for (int i = 1; i <= RUN; i++) {
					OWLOntology ontology = loadOntology(source, filename);
					
					ontology = ontology.getOWLOntologyManager().createOntology(ontology.importsClosure().flatMap(OWLOntology::logicalAxioms).collect(Collectors.toSet()));

					try {
						reasonerConsistencyTime += performConsistencyEvaluation(ontology,
								reasonerFactoryMap.get(reasonerName)); 
						
					}
					catch(Exception e)
					{
						logger.info(reasonerName + " running error " + e.getMessage());
						continue;
					}
					
					// Calling GC 
					System.gc();

				}

				logger.info(reasonerName + " Everage consistency validation time on: " + source + "is: "
						+ reasonerConsistencyTime / (double) RUN);
			}
		}

		logger.info("Evaluating Reasoner Classification");

		for (String reasonerName : reasonerFactoryMap.keySet()) {
			logger.info("");
			logger.info("Evaluation reasoner " + reasonerName);

			for (String source : ontologiesMap.keySet()) {

				String filename = ontologiesMap.get(source);
				logger.info("Ontology: " + source);
				reasonerClassificationTime = 0;

				for (int i = 1; i <= RUN; i++) {
					OWLOntology ontology = loadOntology(source, filename);

					try {
						reasonerClassificationTime += performClassification(ontology, reasonerFactoryMap.get(reasonerName));
					}
					catch(Exception e)
					{
						logger.info(reasonerName + " running error " + e.getMessage());
						continue;
					}
					
					// Calling GC
					System.gc();

				}

				logger.info(reasonerName + " Everage consistency validation time on: " + source + "is: "
						+ reasonerClassificationTime / (double) RUN);
			}
		}
	}
	
	public static long performConsistencyEvaluation(OWLOntology ontology, OWLReasonerFactory reasonerFactory) {
		long startTime = System.currentTimeMillis();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, koncludeReasonerConfiguration);
		boolean consistent = reasoner.isConsistent();
		long endTime = System.currentTimeMillis();
		reasoner.dispose();

		logger.info(
				"Reasoner consistency validation takes " + (endTime - startTime) + " ms. IsConsisten = " + consistent);

		return (endTime - startTime);
	}

	public static long performClassification(OWLOntology ontology, OWLReasonerFactory reasonerFactory) {
		long startTime = System.currentTimeMillis();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, koncludeReasonerConfiguration);
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		long endTime = System.currentTimeMillis();
		reasoner.dispose();

		logger.info("Reasoner classification takes " + (endTime - startTime) + " ms.");

		return (endTime - startTime);
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
		System.out.println("Number of Classes " + numClassses);
		
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

