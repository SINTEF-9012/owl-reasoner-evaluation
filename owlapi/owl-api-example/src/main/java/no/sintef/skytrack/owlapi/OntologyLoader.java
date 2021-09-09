package no.sintef.skytrack.owlapi;

import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import openllet.owlapi.OpenlletReasonerFactory;


public class OntologyLoader {
	
	static Logger logger = LoggerFactory.getLogger(OntologyLoader.class);
	

	public static void main(String[] args) {

		String FILENAME = "../../ontologies/ACGT.owl";
		String SOURCE = "http://www.ifomis.org/acgt/1.0";
		
		

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		IRI iri = IRI.create(SOURCE);
		
		if(FILENAME != null)
		{
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

			
			// String prefix = ontology.getOntologyID().getOntologyIRI().get() + "#";
			
			

			
			 OWLReasoner reasoner = OpenlletReasonerFactory.getInstance().createNonBufferingReasoner(ontology);
			 
			 

			 	long startTime = System.currentTimeMillis();
		        boolean consistent = reasoner.isConsistent();
		        long endTime = System.currentTimeMillis();
		        
		        logger.info("Validation takes " + (endTime - startTime) + " ms"); 
		        
		      
		        if (!consistent) {
		        	logger.info("The Model is NOT consitent");
		        } else {
		        	logger.info("The Model is consitent");
		        }
			 
		}

	}

}
