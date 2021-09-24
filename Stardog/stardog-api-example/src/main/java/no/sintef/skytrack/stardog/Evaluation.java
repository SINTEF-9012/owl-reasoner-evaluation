package no.sintef.skytrack.stardog;

import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.complexible.stardog.CommitResult;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;
import com.stardog.stark.io.RDFFormats;

public class Evaluation {

	static Logger logger = LoggerFactory.getLogger(Evaluation.class);

	public static void main(String[] args) {
		Map<String, String> ontologiesMap = new LinkedHashMap<String, String>();

		ontologiesMap.put("VICODI", "../../../ontologies/vicodi_all.owl");
		ontologiesMap.put("ACGT", "../../../ontologies/ACGT.owl");
		//ontologiesMap.put("GALEN", "../../../ontologies/full-galen.owl");
		//ontologiesMap.put("FMA", "../../../ontologies/fma.owl");
		//ontologiesMap.put("NCIT", "../../../ontologies/ncit.owl");
		//ontologiesMap.put("MESH", "../../../ontologies/MESH.owl");
		//ontologiesMap.put("http://purl.bioontology.org/ontology/MESH/", "../../../ontologies/MESH.ttl");

		int RUN = 5;

		long evaluationTime = 0;
		long validationTime = 0;
		long reasonerLoadingTime = 0;
		long startTime, endTime;

		for (String source : ontologiesMap.keySet()) {

			String filename = ontologiesMap.get(source);
			logger.info("");
			logger.info("Ontology: " + source);
			evaluationTime = 0;
			for (int i = 1; i <= RUN; i++) {
				evaluationTime += loadOntologyEvaluation(source, filename);
			}

			logger.info("Everage Loading Time of " + filename + " :" + evaluationTime / (double) RUN);

		}

	}

	public static long loadOntologyEvaluation(String source, String filename) {

		long startTime = 0, endTime = 0;

		try {

			AdminConnection aAdminConnection = AdminConnectionConfiguration.toServer("http://192.168.1.45:5820")
					.credentials("admin", "admin").connect();

			if (aAdminConnection.list().contains(source)) {
				aAdminConnection.drop(source);
			}

			ConnectionConfiguration connCfg = aAdminConnection.newDatabase(source).create();
			Connection aConn = connCfg.connect();
			aConn.begin();

			startTime = System.currentTimeMillis();

			aConn.add().io().format(RDFFormats.RDFXML).stream(new FileInputStream(filename));

			CommitResult result = aConn.commit();

			endTime = System.currentTimeMillis();
			logger.info(result.toString());

			logger.info("Loading takes " + (endTime - startTime) + " ms");
			
			aConn.close();
			aAdminConnection.close();

		} catch (Exception e) {

			e.printStackTrace();

		}

		return (endTime - startTime);
	}

}
