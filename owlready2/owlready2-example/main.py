import owlready2 as owlready2
import time

if __name__ == '__main__':

    owlready2.onto_path.append("../../ontologies/")
    ns = "http://vicodi.org/ontology#"
    ontology_filename = "vicodi_all.owl"


    print("Loading ontology " + ns)
    onto = owlready2.get_ontology(ontology_filename).load()
    onto.base_iri = ns
    onto_classes = list(onto.classes())
    onto_individuals = list(onto.individuals())
    print("Number of classes:", len(onto_classes))
    print("Number of individual: ", len(onto_individuals))

    #individuals = onto.search(type = onto.Individual)
    #print(individuals)


    print("Starting reasoner")
    with onto:
        owlready2.sync_reasoner(debug=0, infer_property_values=True)

    print("Printing superclasses of the Professor")
    print(onto.Professor.is_a)
    print(onto.get_parents_of(onto.Professor))

    print("Printing names of the Professors")
    for ind in onto.Individual.instances():
        for roles in ind.hasRole:
            for role in roles.is_a:
                if role == onto.Professor:
                    print("->" + str(ind.name))

    print("Saving ontology")
    onto.save(file="../../ontologies/vicodi_owlready2_saving.owl", format="ntriples")

