#!/bin/bash

declare -A ontoArr
ontoArr["gallen.owl"]="http://krr-nas.cs.ox.ac.uk/ontologies/lib/GALEN/full-galen/2008-02-05/sources/full-galen.owl"
ontoArr["ochv.owl"]="https://data.bioontology.org/ontologies/OCHV/submissions/2/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb"
ontoArr["snmi.owl"]="https://data.bioontology.org/ontologies/SNMI/submissions/18/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb"
ontoArr["reto.owl"]="https://data.bioontology.org/ontologies/RETO/submissions/8/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb"
ontoArr["rexo.owl"]="https://data.bioontology.org/ontologies/REXO/submissions/6/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb"
ontoArr["gexo.owl"]="https://data.bioontology.org/ontologies/GEXO/submissions/2/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb"
ontoArr["iobc.owl"]="https://data.bioontology.org/ontologies/IOBC/submissions/25/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb"
ontoArr["hoom.xml"]="https://data.bioontology.org/ontologies/HOOM/submissions/4/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf"
ontoArr["rhmesh.owl"]="https://data.bioontology.org/ontologies/RH-MESH/submissions/3/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb"
ontoArr["fma.owl"]="https://data.bioontology.org/ontologies/FMA/submissions/29/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb"
ontoArr["nifstd.xml"]="https://data.bioontology.org/ontologies/NIFSTD/submissions/27/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf"
ontoArr["upheno.xml"]="https://data.bioontology.org/ontologies/UPHENO/submissions/3/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf"
ontoArr["ncit.owl"]="https://data.bioontology.org/ontologies/NCIT/submissions/110/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb"
ontoArr["MESH.owl"]="https://data.bioontology.org/ontologies/MESH/submissions/22/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb"
ontoArr["dron.xml"]="https://data.bioontology.org/ontologies/DRON/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf"
ontoArr["loinc.owl"]="https://data.bioontology.org/ontologies/LOINC/submissions/21/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb"
ontoArr["chebi.xml"]="https://data.bioontology.org/ontologies/CHEBI/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf"
ontoArr["ncbitaxon.owl"]"https://data.bioontology.org/ontologies/NCBITAXON/submissions/17/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb"
ontoArr["gaz.xml"]="https://data.bioontology.org/ontologies/GAZ/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf"
ontoArr["pr.xml"]="https://data.bioontology.org/ontologies/PR/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf"
ontoArr["cco.xml"]="https://data.bioontology.org/ontologies/CCO/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf"

for key in "${!ontoArr[@]}"; do
	if [ -f "$key" ]; then
		continue
	fi
	
    echo "$key ${ontoArr[$key]}"
    wget "${ontoArr[$key]}" -O "$key"
done