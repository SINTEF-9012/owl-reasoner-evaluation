#!/bin/bash

#declare -a ontoArr=("../../ontologies/vicodi_all.owl"
#					"../../ontologies/ACGT.owl"
#					"../../ontologies/full-galen.owl"
#					"../../ontologies/fma.owl")
#					"../../ontologies/ncit.owl"
#					"../../ontologies/MESH.owl")

NEWLINE=$'\n'
ontoDir="../../ontologies/ontologies/"
RUN=10
rm -rf ./output/

mkdir output
mkdir output/consistency
mkdir output/classification
mkdir output/realization
#for i in `ls -Sr ../../ontologies/*`;

echo "Evaluating reasoner Konclude"


echo
echo "Evaluating Reasoner Realization"
echo
output=""
for i in `ls -Sr ${ontoDir}`;
do
   echo "$i"
   echo
   output="$output$i"
   reasonerClassification=0
   for (( runC=1; runC<=$RUN; runC++ )) 
   do 
		start=$(date +%s.%3N)
		Konclude realization -i "$ontoDir$i" -o "./output/realization/realization_${i}" > ./output/realization.log
		end=$(date +%s.%3N)
		runtime=$( echo "scale=3; $end - $start" | bc -l )
		output="${output},${runtime}"
		reasonerClassification=$(echo "scale=3; $runtime + $reasonerClassification" | bc -l)
		echo "Realization takes ${runtime}"
	done
	output="${output}${NEWLINE}"
	
	reasonerClassification=$(echo "scale=3; $reasonerClassification/$RUN" | bc -l)
	echo "Everage Realization time on: $i is $reasonerClassification"
done

echo "$output" > ./output/realization.csv

echo
echo "Evaluating Reasoner Classification"
echo
output=""
for i in `ls -Sr ${ontoDir}`;
do
   echo "$i"
   echo
   output="$output$i"
   reasonerClassification=0
   for (( runC=1; runC<=$RUN; runC++ )) 
   do 
		start=$(date +%s.%3N)
		Konclude classification -i "$ontoDir$i" -o "./output/classification/classification_${i}" > ./output/classification.log
		end=$(date +%s.%3N)
		runtime=$( echo "scale=3; $end - $start" | bc -l )
		output="${output},${runtime}"
		reasonerClassification=$(echo "scale=3; $runtime + $reasonerClassification" | bc -l)
		echo "Classification takes ${runtime}"
	done
	output="${output}${NEWLINE}"
	
	reasonerClassification=$(echo "scale=3; $reasonerClassification/$RUN" | bc -l)
	echo "Everage classification time on: $i is $reasonerClassification"
done

echo "$output" > ./output/classification.csv


echo
echo "Evaluating Reasoner consistency validation"
echo
output=""
#for i in "${ontoArr[@]}"
for i in `ls -Sr ${ontoDir}`;
do
   echo "$i"
   echo
   output="$output$i"
   reasonerConsistencyTime=0
   for (( runC=1; runC<=$RUN; runC++ )) 
   do 
		start=$(date +%s.%3N)
		Konclude consistency -i "$ontoDir$i" -o "./output/consistency/consistency_${i}" > ./output/consistency.log
		end=$(date +%s.%3N)
		runtime=$( echo "scale=3; $end - $start" | bc -l )
		output="${output},${runtime}"
		reasonerConsistencyTime=$(echo "scale=3; $runtime + $reasonerConsistencyTime" | bc -l)
		echo "Consistency validation takes ${runtime}"
	done
	output="${output}${NEWLINE}"
	
	reasonerConsistencyTime=$(echo "scale=3; $reasonerConsistencyTime/$RUN" | bc -l)
	echo "Everage consistency validation time on: $i is $reasonerConsistencyTime"
done

echo "$output" > ./output/consistency.csv