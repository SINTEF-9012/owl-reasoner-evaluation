import owlready2 as owlready2
import time

if __name__ == '__main__':

    ns = "http://vicodi.org/ontology#"

    print("Loading ontology " + ns)
    start_time = time.time()
    onto = owlready2.get_ontology("file://../../ontologies/vicodi_all.owl").load()
    end_time = time.time()

    print("Loading takes ", end_time - start_time)

    onto_classes = list(onto.classes())
    onto_individuals = list(onto.individuals())
    print("Number of classes:", len(onto_classes))
    print("Number of individual: ", len(onto_individuals))



