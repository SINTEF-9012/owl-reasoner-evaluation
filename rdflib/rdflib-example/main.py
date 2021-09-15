import rdflib
import owlrl
import time


if __name__ == '__main__':
    ns = "http://vicodi.org/ontology#"
    ontology_filename = "../../ontologies/vicodi_all.owl"

    print("Loading ontology: " + ns)

    rdflib.logger.setLevel('FATAL')


    g = rdflib.Graph()
    start_time = time.time()
    g.parse(ontology_filename)
    end_time = time.time()
    print("Loading Time: ", end_time - start_time)



    print("Materializing the ontology")
    start_time = time.time()
    owlrl.DeductiveClosure(owlrl.OWLRL_Semantics).expand(g)
    end_time = time.time()
    print("Time: ", end_time - start_time)

    q1 = """
            PREFIX : <http://vicodi.org/ontology#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            SELECT ?s
            WHERE {
                :Professor rdfs:subClassOf ?s .
            }
            """

    print("Printing superclasses of the Professor")
    for r in g.query(q1):
        print("->" + r["s"])

    q2 = """
            PREFIX : <http://vicodi.org/ontology#>
            SELECT ?s
            WHERE {
                ?s a :Individual .
                ?s :hasRole ?r .
                ?r a :Professor .
            }
            """
    print("Printing names of the Professors")
    for r in g.query(q2):
        print("->" + r["s"])

    g.serialize("../../ontologies/vicodi_rdflib_saving.owl", format="turtle")