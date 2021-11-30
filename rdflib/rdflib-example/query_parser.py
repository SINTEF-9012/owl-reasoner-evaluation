import rdflib
import owlrl
import time

from rdflib.plugins.sparql import prepareQuery

if __name__ == '__main__':


    q1 = """
            PREFIX : <http://vicodi.org/ontology#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            SELECT ?s
            WHERE {
                :Professor rdfs:subClassOf ?s .
            }
            """

    query = prepareQuery(q1)

    print(query.prologue)

