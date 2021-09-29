#!/bin/bash

declare -a ontoArr=("../../ontologies/vicodi_all.owl"
					"../../ontologies/ACGT.owl"
					"../../ontologies/full-galen.owl"
					"../../ontologies/fma.owl"
					"../../ontologies/ncit.owl"
					"../../ontologies/MESH.owl")

echo "Evaluating reasoner Konclude"
echo
echo "Evaluating Reasoner consistency validation"
echo
for i in "${ontoArr[@]}"
do
   echo "$i"
   echo
   reasonerConsistencyTime=0
   for runC in {1..5}; do
		start=$(date +%s.%3N)
		Konclude consistency -i $i -o ./output/consistency_check.owl.xml > consistency_check.log
		end=$(date +%s.%3N)
		runtime=$( echo "$end - $start" | bc -l )
		reasonerConsistencyTime=$(echo "$runtime + $reasonerConsistencyTime" | bc -l)
		echo "Consistency validation takes ${runtime}"
	done
	
	reasonerConsistencyTime=$(echo "$reasonerConsistencyTime/5" | bc -l)
	echo "Everage consistency validation time on: $i is $reasonerConsistencyTime"
done



echo "Evaluating Reasoner Classification"
for i in "${ontoArr[@]}"
do
   echo "$i"
   echo
   reasonerConsistencyTime=0
   for runC in {1..5}; do
		start=$(date +%s.%3N)
		Konclude classification -i $i -o ./output/classification.owl.xml > classification.log
		end=$(date +%s.%3N)
		runtime=$( echo "$end - $start" | bc -l )
		reasonerConsistencyTime=$(echo "$runtime + $reasonerConsistencyTime" | bc -l)
		echo "Classification takes ${runtime}"
	done
	
	reasonerConsistencyTime=$(echo "$reasonerConsistencyTime/5" | bc -l)
	echo "Everage classification time on: $i is $reasonerConsistencyTime"
done
