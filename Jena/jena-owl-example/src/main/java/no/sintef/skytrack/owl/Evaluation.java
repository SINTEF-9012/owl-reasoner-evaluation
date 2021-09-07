package no.sintef.skytrack.owl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

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
		
		
		// String FILENAME = "file:ontologies/fma.owl";

		// String SOURCE = "http://www.co-ode.org/ontologies/galen";
		//String FILENAME = "file:ontologies/full-galen.owl";
		String FILENAME = "file:../ontologies/pizza.owl";
		String SOURCE = "http://www.co-ode.org/ontologies/galen";
		
		//String SOURCE = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
		
		
		if(args != null && args.length > 1)
		{
			SOURCE = args[0];
			FILENAME = "file:" + args[1];
		}
		
		//String SOURCE = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
		//String FILENAME = "file:ontologies/pizza.owl";
		
		
		String NS = SOURCE + "#";
		
		OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		OntDocumentManager dm = base.getDocumentManager();

		if (FILENAME != null)
			dm.addAltEntry(SOURCE, FILENAME);

		logger.info("Loading the ontology " + SOURCE);

		long startTime = System.currentTimeMillis();
		base.read(SOURCE);
		long endTime = System.currentTimeMillis();

		logger.info("Loading takes " + (endTime - startTime) + " ms");

		int numClassses = base.listNamedClasses().toList().size();
		int numIndividual = base.listIndividuals().toList().size();

		logger.info("Number of Classes " + numClassses);
		logger.info("Number of Individual " + numIndividual);

		Reasoner owlReasoner = ReasonerRegistry.getOWLReasoner();
		Reasoner owlMiniReasoner = ReasonerRegistry.getOWLMiniReasoner();
		Reasoner owlMicroReasoner = ReasonerRegistry.getOWLMicroReasoner();
		
		
		logger.info("Evaluating OWL Micro reasoner");
		PerformTest(base, owlMicroReasoner);
		
		logger.info("Evaluating OWL Mini reasoner");
		PerformTest(base, owlMiniReasoner);
		
		logger.info("Evaluating OWL reasoner");
		PerformTest(base, owlReasoner);
		
		
	}

	
	public static OntModel PerformTest(OntModel base, Reasoner reasoner) {

		logger.info("Building the inference ontology");
		

		long startTime = System.currentTimeMillis();
		InfModel infmodel = ModelFactory.createInfModel(reasoner, base);
		infmodel.prepare();
		long endTime = System.currentTimeMillis();

		logger.info("Materialization takes " + (endTime - startTime) + " ms");


		logger.info("Validating the ontology");
		startTime = System.currentTimeMillis();
		ValidityReport validateReport = infmodel.validate();
		endTime = System.currentTimeMillis();

		logger.info("Validation takes " + (endTime - startTime) + " ms");

		if (validateReport.isValid()) {
			logger.info("The Model is valid");
		}
		else
			logger.info("The Model is not valid");

		Iterator<Report> reports = validateReport.getReports();
		while (reports.hasNext())
			logger.info(reports.next().toString());

		return base;
	}

}
