package no.sintef.skytrack.jena;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.ValidityReport.Report;
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.ReasonerVocabulary;

public class OntologyLoader {

	public static void main(String[] args) {

		// String SOURCE = "http://purl.org/sig/ont/fma.owl";
		// String FILENAME = "file:ontologies/fma.owl";

		// String SOURCE = "http://www.co-ode.org/ontologies/galen";
		// String FILENAME = "file:ontologies/full-galen.owl";

		String SOURCE = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
		String FILENAME = "file:../ontologies/pizza.owl";

		String NS = SOURCE + "#";

		OntModel base = loadOntology(SOURCE, FILENAME);

		String queryString = "PREFIX pizza:      <http://www.co-ode.org/ontologies/pizza/pizza.owl#>\r\n"
				+ "PREFIX rdfs:      <http://www.w3.org/2000/01/rdf-schema#>\r\n" + "\r\n"
				+ "SELECT ?namePizza ?label\r\n" + "WHERE\r\n" + "{ \r\n"
				+ "	?namePizza rdfs:subClassOf pizza:NamedPizza .\r\n" + "	?namePizza rdfs:label ?label .\r\n"
				+ "	FILTER(lang(?label) = \"en\")\r\n" + "}";

		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, base);
		ResultSet results = qe.execSelect();

		// Output query results
		ResultSetFormatter.out(System.out, results, query);

		// Important free up resources used running the query
		qe.close();

		queryString = "PREFIX pizza:      <http://www.co-ode.org/ontologies/pizza/pizza.owl#>\n"
				+ "PREFIX rdfs:      <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "CONSTRUCT {?namePizza rdfs:label ?label }\n" + "WHERE\n" + "{ \n"
				+ "	?namePizza rdfs:subClassOf pizza:NamedPizza .\n" + "	?namePizza rdfs:label ?label .\n"
				+ "	FILTER(lang(?label) = \"en\")\n" + "}";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, base);
		Model resultModel = qe.execConstruct();
		try {
			resultModel.write(new FileWriter("ontologies/pizza_construct.ttl"), "TURTLE");
			resultModel.write(new FileWriter("ontologies/pizza_construct.json"), "JSONLD");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		queryString = "PREFIX pizza:      <http://www.co-ode.org/ontologies/pizza/pizza.owl#>\r\n"
				+ "PREFIX rdfs:      <http://www.w3.org/2000/01/rdf-schema#>\r\n" + "\r\n" + "DESCRIBE  ?namePizza\r\n"
				+ "WHERE\r\n" + "{ \r\n" + "	?namePizza rdfs:subClassOf pizza:NamedPizza .\r\n" + "}";

		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, base);
		resultModel = qe.execDescribe();
		try {
			resultModel.write(new FileWriter("ontologies/pizza_describe.ttl"), "TURTLE");
			resultModel.write(new FileWriter("ontologies/pizza_describe.json"), "JSONLD");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintUtil.registerPrefix("pizza", NS);

		Property pFavPizza = base.createDatatypeProperty(NS + "myFavPizza", false);
		Resource configuration = base.createResource();
		configuration.addProperty(ReasonerVocabulary.PROPruleMode, "hybrid");
		configuration.addProperty(ReasonerVocabulary.PROPruleSet, "rules/pizza.rules");
		Reasoner reasoner = GenericRuleReasonerFactory.theInstance().create(configuration);
		InfModel infmodel = ModelFactory.createInfModel(reasoner, base);

		StmtIterator i = infmodel.listStatements(null, pFavPizza, (RDFNode) null);
		while (i.hasNext()) {
			System.out.println(" - " + PrintUtil.print(i.nextStatement()));
		}

		/*
		 * try { infmodel.getDeductionsModel().write(new
		 * FileWriter("ontologies/rule_ontology.ttl"), "TURTLE");
		 * 
		 * } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
	}

	public static OntModel loadOntology(String source, String filePath) {
		OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		OntDocumentManager dm = base.getDocumentManager();

		if (filePath != null)
			dm.addAltEntry(source, filePath);

		System.out.println("Loading the ontology " + source);
		base.read(source);

		int numClassses = base.listNamedClasses().toList().size();
		int numIndividual = base.listIndividuals().toList().size();

		System.out.println("Number of Classes " + numClassses);
		System.out.println("Number of Individual " + numIndividual);

		System.out.println("Writing the base ontologies");

		try {
			base.write(new FileWriter("ontologies/base_ontology.ttl"), "TURTLE");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Building the inference ontology");
		

		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		// reasoner = reasoner.bindSchema(base);

		long startTime = System.currentTimeMillis();
		InfModel infmodel = ModelFactory.createInfModel(reasoner, base);
		infmodel.prepare();
		long endTime = System.currentTimeMillis();

		System.out.println("Materialization takes " + (endTime - startTime) + " ms");

		//

		// OntModel inf =
		// ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, base);

		//OntModel inf = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, base);

		// inf.prepare();

		System.out.println("Validating the ontology");

		startTime = System.currentTimeMillis();

		ValidityReport validateReport = infmodel.validate();

		endTime = System.currentTimeMillis();

		System.out.println("Validation takes " + (endTime - startTime) + " ms");

		if (validateReport.isValid()) {
			System.out.println("The Model is valid");

			System.out.println("Writing the materalized ontologies");
			try {
				infmodel.write(new FileWriter("ontologies/materialized_ontology.ttl"), "TURTLE");

			} catch (IOException e) { // TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		else
			System.out.println("The Model is not valid");

		Iterator<Report> reports = validateReport.getReports();
		while (reports.hasNext())
			System.out.println(reports.next());

		return base;
	}

}
