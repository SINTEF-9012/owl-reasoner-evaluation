#!/bin/bash

declare -a ontoArr=("../../ontologies/vicodi_all.owl"
					"../../ontologies/ACGT.owl"
					"../../ontologies/full-galen.owl"
					"../../ontologies/fma.owl")
#					"../../ontologies/ncit.owl"
#					"../../ontologies/MESH.owl")

#for i in `ls -Sr ../../ontologies/*`;

echo "Evaluating reasoner Konclude"
echo
echo "Evaluating Reasoner consistency validation"
echo
output=""
for i in "${ontoArr[@]}"
do
   echo "$i"
   echo
   output="$output$i"
   reasonerConsistencyTime=0
   for runC in {1..10}; do
		start=$(date +%s%3N)
		Konclude consistency -i $i -o ./output/consistency.owl.xml > ./output/consistency.log
		end=$(date +%s%3N)
		runtime=$( echo "$end - $start" | bc -l )
		output="${output},${runtime}"
		reasonerConsistencyTime=$(echo "$runtime + $reasonerConsistencyTime" | bc -l)
		echo "Consistency validation takes ${runtime}"
	done
	output="${output}\n"
	
	reasonerConsistencyTime=$(echo "$reasonerConsistencyTime/5" | bc -l)
	echo "Everage consistency validation time on: $i is $reasonerConsistencyTime"
done

echo "$output" >> ./output/consistency.csv



echo
echo "Evaluating Reasoner Classification"
echo
for i in "${ontoArr[@]}"
do
   echo "$i"
   echo
   reasonerClassification=0
   for runC in {1..10}; do
		start=$(date +%s%3N)
		Konclude classification -i $i -o ./output/classification.owl.xml > ./output/classification.log
		end=$(date +%s%3N)
		runtime=$( echo "$end - $start" | bc -l )
		reasonerClassification=$(echo "$runtime + $reasonerClassification" | bc -l)
		echo "Classification takes ${runtime}"
	done
	
	reasonerClassification=$(echo "$reasonerClassification/5" | bc -l)
	echo "Everage classification time on: $i is $reasonerClassification"
done
