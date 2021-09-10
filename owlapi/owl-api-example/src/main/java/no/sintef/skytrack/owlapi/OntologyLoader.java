package no.sintef.skytrack.owlapi;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OntologyLoader {

	static Logger logger = LoggerFactory.getLogger(OntologyLoader.class);

	public static void main(String[] args) {

		String FILENAME = "../../ontologies/vicodi_all.owl";
		String SOURCE = "http://vicodi.org/ontology";
		String NS = SOURCE + "#";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		IRI iri = IRI.create(SOURCE);

		if (FILENAME != null) {
			iri = IRI.create(new File(FILENAME));
		}

		OWLOntology ontology = null;

		try {

			logger.info("Loading the ontology " + SOURCE);
			long startTime = System.currentTimeMillis();
			ontology = manager.loadOntology(iri);
			long endTime = System.currentTimeMillis();

			logger.info("Loading takes " + (endTime - startTime) + " ms");

		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (ontology != null) {

			int numClassses = ontology.getClassesInSignature(Imports.INCLUDED).size();
			System.out.println("Number of Classes " + numClassses);

			OWLReasoner reasoner = new org.semanticweb.HermiT.ReasonerFactory().createReasoner(ontology);

			long startTime = System.currentTimeMillis();
			boolean consistent = reasoner.isConsistent();
			long endTime = System.currentTimeMillis();

			logger.info("Validation takes " + (endTime - startTime) + " ms");

			if (!consistent) {
				logger.info("The Model is NOT consitent");
			} else {
				logger.info("The Model is consitent");
			}
			
			
			 OWLDataFactory fac = manager.getOWLDataFactory();
			 OWLClass individualClass = fac.getOWLClass(NS + "Individual");
			 OWLObjectProperty hasRole = fac.getOWLObjectProperty(NS + "hasCategory");
			 
			 NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(individualClass, false);
			 for(Node<OWLNamedIndividual> ind : individuals)
			 {
				 //System.out.println(ind.toString());
				 ind.getEntities().forEach(x -> reasoner.getObjectPropertyValues(x, hasRole).forEach(y -> System.out.println(ind.toString() + " -> " + y)));
			 }

		}		
		
		logger.info("Saving ontology");
		
		File file = new File("../../ontologies/vicodi_saving.owl");
		try {
			//manager.saveOntology(ontology,  new OWLXMLDocumentFormat(), IRI.create(file.toURI()));
			manager.saveOntology(ontology,  new ManchesterSyntaxDocumentFormat(), IRI.create(file.toURI()));
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
