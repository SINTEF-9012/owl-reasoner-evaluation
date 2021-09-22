import owlready2
import time

def eval_load_ontology(location):
    start_time = time.time()
    onto = owlready2.get_ontology(location).load(reload=True);
    end_time = time.time()
    print("Loading Time: ", end_time - start_time)
    return end_time - start_time

def perform_loading_evaluation(RUN=5):
      for ontology_name, ontology_file in ontologies_map.items():
        print("Ontology " + ontology_name)
        evaluation_time = 0
        for x in range(RUN):
            evaluation_time += eval_load_ontology(ontology_file)

        print("Everage loadding time of " + ontology_name + ": ", evaluation_time / RUN)
        print("")

def load_ontology(location):
    print("Loading " + location)
    onto = owlready2.get_ontology(location).load(reload=True)
    onto_classes = list(onto.classes())
    onto_individuals = list(onto.individuals())
    print("Number of classes:", len(onto_classes))
    print("Number of individual: ", len(onto_individuals))
    return onto

def perform_pellet_reasoner(ontology):
    print("Loading pellet reasoner")
    with ontology:
        start_time = time.time()
        owlready2.sync_reasoner_pellet(debug=0)
        end_time = time.time()

    print("Run Time: ", end_time - start_time)
    return end_time - start_time


def perform_hermit_reasoner(ontology):
    print("Loading hermit reasoner")
    with ontology:
        start_time = time.time()
        owlready2.sync_reasoner_hermit(debug=0)
        end_time = time.time()

    print("Run Time: ", end_time - start_time)
    return end_time - start_time

def perform_reasoning_evaluation():
    RUN = 5
    for ontology_name, ontology_file in ontologies_map.items():
        print("Ontology " + ontology_name)
        evaluation_time = 0
        for x in range(RUN):
            onto = load_ontology(ontology_file)
            onto.base_iri = ontology_name
            evaluation_time += perform_hermit_reasoner(onto)

        print("Everage runtime of HermiT ", evaluation_time / RUN)
        print("")

        evaluation_time = 0
        for x in range(RUN):
            onto = load_ontology(ontology_file)
            evaluation_time += perform_pellet_reasoner(onto)

        print("Everage runtime of Pellet ", evaluation_time / RUN)
        print("")

if __name__ == '__main__':

    ontologies_map = {
        #"http://vicodi.org/ontology": "vicodi_all.owl",
        #"http://www.ifomis.org/acgt/1.0": "ACGT.owl",
        "http://www.co-ode.org/ontologies/galen": "full-galen.owl",
        "http://purl.org/sig/ont/fma.owl": "fma.owl",
        "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#":  "ncit.owl",
         "http://purl.bioontology.org/ontology/MESH/": "MESH.owl"

    }

    owlready2.onto_path.append("../../ontologies/")
    owlready2.reasoning.JAVA_MEMORY = 5000

    #perform_loading_evaluation()
    perform_reasoning_evaluation()





