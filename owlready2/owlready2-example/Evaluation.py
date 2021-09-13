import owlready2 as owlready2
import time

def eval_load_ontology(location):
    start_time = time.time()
    onto = owlready2.get_ontology("file://../../ontologies/vicodi_all.owl").load()
    end_time = time.time()
    return end_time - start_time


if __name__ == '__main__':

    ontologies_map = {
        "http://vicodi.org/ontology": "file://../../ontologies/vicodi_all.owl",
        "http://www.ifomis.org/acgt/1.0": "file://../../ontologies/ACGT.owl",
        "http://www.co-ode.org/ontologies/galen": "file://../../ontologies/full-galen.owl"
    }

    RUN = 5
    evaluation_time = 0

    for ontology_name, ontology_file in ontologies_map.items():
        print("Loading ontology " + ontology_name)
        for x in range(RUN):
            print(x)



    onto_classes = list(onto.classes())
    onto_individuals = list(onto.individuals())
    print("Number of classes:", len(onto_classes))
    print("Number of individual: ", len(onto_individuals))