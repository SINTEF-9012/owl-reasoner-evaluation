package no.sintef.skytrack.owl;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.ShaclValidator;

public class ShaclExample {

	public static void main(String[] args) {
		String SHAPES = "shapes.ttl";
		String DATA = "data.ttl";

		Graph shapesGraph = RDFDataMgr.loadGraph(SHAPES);
		Graph dataGraph = RDFDataMgr.loadGraph(DATA);

		Shapes shapes = Shapes.parse(shapesGraph);

		ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);
		ShLib.printReport(report);
		//System.out.println();
		//RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
	}
}
