package no.sintef.skytrack.jena.openllet;

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

import openllet.jena.PelletReasonerFactory;

public class Evaluation {
	
	static Logger logger = LoggerFactory.getLogger(Evaluation.class);

	public static void main(String[] args) {
		
		Map<String, String> ontologiesMap = new LinkedHashMap<String, String>();
		ontologiesMap.put("http://vicodi.org/ontology", "file:../../ontologies/vicodi_all.owl");
		ontologiesMap.put("http://www.ifomis.org/acgt/1.0", "file:../../ontologies/ACGT.owl");
		ontologiesMap.put("http://www.co-ode.org/ontologies/galen", "file:../../ontologies/full-galen.owl");
		
		Map<String, Reasoner> reasonerFactoryMap = new LinkedHashMap<>();
		reasonerFactoryMap.put("Pellet",  PelletReasonerFactory.theInstance().create());
		
		
		int RUN = 5;
		long evaluationTime = 0;


		logger.info("");
		logger.info("Evaluating Reasoner");
		
		for(String source : ontologiesMap.keySet())
		{
			
			String filename = ontologiesMap.get(source);
			logger.info("Ontology: " + source);
			
			
			for(String reasonerName : reasonerFactoryMap.keySet())
			{
				logger.info("");
				logger.info("Evaluation reasoner " + reasonerName);
				evaluationTime = 0;
				
				for(int i = 1; i <= RUN; i++)
				{
					OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
					OntDocumentManager dm = base.getDocumentManager();
					dm.addAltEntry(source, filename);
					base.read(source);
					
					Reasoner reasoner = reasonerFactoryMap.get(reasonerName);
					
					evaluationTime += performValidationTest(base, reasoner);
				}
				
				logger.info(reasonerName + " Everage Evaluation Time: " + evaluationTime/(double)RUN);
			}
			
		}
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
