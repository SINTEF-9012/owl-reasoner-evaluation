import jnius_config
jnius_config.add_classpath("../libs/*")

from jnius import autoclass

# Press the green button in the gutter to run the script.
if __name__ == '__main__':


    autoclass('java.lang.System').out.println('Hello world!')

    OWLOntologyManager = autoclass("org.semanticweb.owlapi.model.OWLOntologyManager")
    OWLManager = autoclass("org.semanticweb.owlapi.apibinding.OWLManager")
    OWLOntology = autoclass("org.semanticweb.owlapi.model.OWLOntology")
    OWLOntologyManagerImpl = autoclass("uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl")

    print(dir(OWLOntologyManagerImpl))

    #Error here, cannot initialize the manager
    manager = OWLManager.createOWLOntologyManager()


