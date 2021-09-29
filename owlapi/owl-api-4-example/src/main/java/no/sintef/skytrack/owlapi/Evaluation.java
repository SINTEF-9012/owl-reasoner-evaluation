package no.sintef.skytrack.owlapi;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Evaluation {

	static Logger logger = LoggerFactory.getLogger(Evaluation.class);

	public static void main(String[] args) {

		
		logger.info("");
		logger.info("--------------------------------------------------");
		logger.info("");
		
		
		System.out.println(System.getProperty("java.library.path"));

		Map<String, String> ontologiesMap = new LinkedHashMap<String, String>();
		ontologiesMap.put("http://vicodi.org/ontology#", "../../../ontologies/vicodi_all.owl");
		ontologiesMap.put("http://www.ifomis.org/acgt/1.0#", "../../../ontologies/ACGT.owl");
		ontologiesMap.put("http://www.co-ode.org/ontologies/galen#", "../../../ontologies/full-galen.owl");
		ontologiesMap.put("http://purl.org/sig/ont/fma.owl#", "../../../ontologies/fma.owl");
		ontologiesMap.put("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#", "../../../ontologies/ncit.owl");
		ontologiesMap.put("http://purl.bioontology.org/ontology/MESH/", "../../../ontologies/MESH.owl");
		// ontologiesMap.put("http://purl.bioontology.org/ontology/MESH/",
		// "../../../ontologies/MESH.ttl");

		Map<String, OWLReasonerFactory> reasonerFactoryMap = new LinkedHashMap<>();
		reasonerFactoryMap.put("ELK", new org.semanticweb.elk.owlapi.ElkReasonerFactory());
		reasonerFactoryMap.put("Fact++", new uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory());
		
		int RUN = 5;

		long validationTime, startTime, endTime, reasonerLoadingTime;
		long reasonerClassificationTime, reasonerConsistencyTime;

		/*
		 * logger.info("Evaluating Reasoner");
		 * 
		 * for (String reasonerName : reasonerFactoryMap.keySet()) { logger.info("");
		 * logger.info("Evaluation reasoner " + reasonerName);
		 * 
		 * for (String source : ontologiesMap.keySet()) {
		 * 
		 * String filename = ontologiesMap.get(source); logger.info("Ontology: " +
		 * source); validationTime = 0; reasonerLoadingTime = 0;
		 * 
		 * for (int i = 1; i <= RUN; i++) { OWLOntology ontology = loadOntology(source,
		 * filename); startTime = System.currentTimeMillis(); OWLReasoner reasoner =
		 * reasonerFactoryMap.get(reasonerName).createReasoner(ontology); endTime =
		 * System.currentTimeMillis(); logger.info("Reasoner Loading takes " + (endTime
		 * - startTime) + " ms");
		 * 
		 * reasonerLoadingTime += (endTime - startTime);
		 * 
		 * validationTime += performEvaluation(ontology, reasoner).get(0);
		 * 
		 * // Calling GC System.gc();
		 * 
		 * }
		 * 
		 * logger.info(reasonerName + " Everage Loading Time on: " + source + "is: " +
		 * reasonerLoadingTime / (double) RUN);
		 * 
		 * logger.info(reasonerName + " Everage Validation Time on: " + source + "is: "
		 * + validationTime / (double) RUN); }
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

					reasonerConsistencyTime += performConsistencyEvaluation(ontology,
							reasonerFactoryMap.get(reasonerName)); 
					
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

					reasonerClassificationTime += performClassification(ontology, reasonerFactoryMap.get(reasonerName));
					// Calling GC
					System.gc();

				}

				logger.info(reasonerName + " Everage consistency validation time on: " + source + "is: "
						+ reasonerClassificationTime / (double) RUN);
			}
		}

	}

	public static OWLOntology loadOntology(String source, String filename) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IRI iri = IRI.create(source);

		if (filename != null) {
			iri = IRI.create(new File(filename));
		}

		OWLOntology ontology = null;

		try {

			//logger.info("Loading the ontology " + source);
			//long startTime = System.currentTimeMillis();
			ontology = manager.loadOntology(iri);
			//long endTime = System.currentTimeMillis();

			//logger.info("Loading takes " + (endTime - startTime) + " ms");

		} catch (OWLOntologyCreationException e) {

			e.printStackTrace();
		}

		int numClassses = ontology.getClassesInSignature(Imports.INCLUDED).size();
		//System.out.println("Number of Classes " + numClassses);

		return ontology;
	}
	
	public static long performConsistencyEvaluation(OWLOntology ontology, OWLReasonerFactory reasonerFactory) {
		long startTime = System.currentTimeMillis();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
		boolean consistent = reasoner.isConsistent();
		long endTime = System.currentTimeMillis();
		reasoner.dispose();

		logger.info(
				"Reasoner consistency validation takes " + (endTime - startTime) + " ms. IsConsisten = " + consistent);

		return (endTime - startTime);
	}

	public static long performClassification(OWLOntology ontology, OWLReasonerFactory reasonerFactory) {
		long startTime = System.currentTimeMillis();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		long endTime = System.currentTimeMillis();
		reasoner.dispose();

		logger.info("Reasoner classification takes " + (endTime - startTime) + " ms.");

		return (endTime - startTime);
	}

	public static long loadOntologyEvaluation(String source, String filename) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IRI iri = IRI.create(source);

		if (filename != null) {
			iri = IRI.create(new File(filename));
		}

		long startTime = 0, endTime = 0;

		try {

			// logger.info("Loading the ontology " + source);
			startTime = System.currentTimeMillis();
			OWLOntology ontology = manager.loadOntology(iri);
			endTime = System.currentTimeMillis();

			logger.info("Loading takes " + (endTime - startTime) + " ms");

		} catch (OWLOntologyCreationException e) {

			e.printStackTrace();
		}

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
			// logger.info("The Model is NOT consitent");
		} else {
			// logger.info("The Model is consitent");
		}

		return ret;

	}

}
