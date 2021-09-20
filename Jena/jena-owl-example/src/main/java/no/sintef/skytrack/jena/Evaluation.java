package no.sintef.skytrack.jena;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.ValidityReport.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Evaluation {

	static Logger logger = LoggerFactory.getLogger(Evaluation.class);

	public static void main(String[] args) {

		Map<String, String> ontologiesMap = new LinkedHashMap<String, String>();
		//ontologiesMap.put("http://vicodi.org/ontology#", "file:../../../ontologies/vicodi_all.owl");
		//ontologiesMap.put("http://www.ifomis.org/acgt/1.0#", "file:../../../ontologies/ACGT.owl");
		//ontologiesMap.put("http://www.co-ode.org/ontologies/galen#", "file:../../../ontologies/full-galen.owl");
		//ontologiesMap.put("http://purl.org/sig/ont/fma.owl#", "file:../../../ontologies/fma.owl");
		//ontologiesMap.put("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#", "file:../../../ontologies/ncit.owl");
		ontologiesMap.put("http://purl.bioontology.org/ontology/MESH/", "file:../../../ontologies/MESH.ttl");
		//ontologiesMap.put("http://purl.obolibrary.org/obo/gaz.owl#", "file:../../../ontologies/gaz.owl");
		
		Map<String, Reasoner> reasonerFactoryMap = new LinkedHashMap<>();
		reasonerFactoryMap.put("OWL Micro",  ReasonerRegistry.getOWLMicroReasoner());
		reasonerFactoryMap.put("OWL Mini",ReasonerRegistry.getOWLMiniReasoner());
		reasonerFactoryMap.put("OWL", ReasonerRegistry.getOWLReasoner());
		
		

		int RUN = 1;

		long evaluationTime = 0;

		for (String source : ontologiesMap.keySet()) {

			String filename = ontologiesMap.get(source);
			logger.info("");
			logger.info("Ontology: " + source);
			evaluationTime = 0;
			for (int i = 1; i <= RUN; i++) {
				evaluationTime += performLoadingOntologyTest(source, filename);
			}

			logger.info("Everage Loading Time of " + filename + " :" + evaluationTime / (double) RUN);

		}

		/*
		 * logger.info(""); logger.info("Evaluating Reasoner");
		 * 
		 * for(String source : ontologiesMap.keySet()) {
		 * 
		 * String filename = ontologiesMap.get(source); logger.info("Ontology: " +
		 * source);
		 * 
		 * 
		 * for(String reasonerName : reasonerFactoryMap.keySet()) { logger.info("");
		 * logger.info("Evaluation reasoner " + reasonerName); evaluationTime = 0;
		 * 
		 * for(int i = 1; i <= RUN; i++) { OntModel base =
		 * ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); OntDocumentManager dm
		 * = base.getDocumentManager(); dm.addAltEntry(source, filename);
		 * base.read(source);
		 * 
		 * Reasoner reasoner = reasonerFactoryMap.get(reasonerName);
		 * 
		 * evaluationTime += performValidationTest(base, reasoner); }
		 * 
		 * logger.info(reasonerName + " Everage Evaluation Time: " +
		 * evaluationTime/(double)RUN); }
		 * 
		 * }
		 */
	}

	public static long performLoadingOntologyTest(String source, String filename) {
		OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		OntDocumentManager dm = base.getDocumentManager();
		dm.addAltEntry(source, filename);

		long startTime = System.currentTimeMillis();
		if(filename.endsWith("ttl"))
			base.read(source, "TTL");
		else
			base.read(source);
		long endTime = System.currentTimeMillis();
		
		
		try {

			logger.info("Writing mesh file");
			base.write(new FileWriter("../../../ontologies/MESH.owl"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int numClassses = base.listNamedClasses().toList().size();
		int numIndividual = base.listIndividuals().toList().size();

		System.out.println("Number of Classes " + numClassses);
		System.out.println("Number of Individual " + numIndividual);

		logger.info("Loading takes " + (endTime - startTime) + " ms");

		return endTime - startTime;
	}

	public static long performValidationTest(OntModel base, Reasoner reasoner) {

		InfModel infmodel = ModelFactory.createInfModel(reasoner, base);
		
		infmodel.prepare();

	
		logger.info("Validating the ontology");
		long startTime = System.currentTimeMillis();
		ValidityReport validateReport = infmodel.validate();
		long endTime = System.currentTimeMillis();

		logger.info("Validation takes " + (endTime - startTime) + " ms");

		if (validateReport.isValid()) {
			logger.info("The Model is valid");
		} else
			logger.info("The Model is not valid");

		Iterator<Report> reports = validateReport.getReports();
		while (reports.hasNext())
			logger.info(reports.next().toString());

		return endTime - startTime;
	}

}
