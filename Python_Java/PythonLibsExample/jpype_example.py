import jpype
import jpype.imports
from jpype.types import *
from jpype import *
import jpype._core


if __name__ == '__main__':
    jpype._core.startJVM(classpath=["../libs/*"])

    import java

    java.lang.System.out.println('Hello World!')


    from org.semanticweb.owlapi.apibinding import OWLManager
    from org.semanticweb.owlapi.model import OWLOntologyManager
    from org.semanticweb.owlapi.model import OWLOntology

    # Error here, cannot initialize the manager
    manager = OWLManager.createOWLOntologyManager()