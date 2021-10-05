#!/bin/bash

NEWLINE=$'\n'
ontoDir="../../ontologies/ontologies/"
RUN=10
#rm -rf ./output/

consistency_task=false
classification_task=false
realization_task=false
skip_to_file=""

while getopts j:vcr flag
do
    case "${flag}" in
        j) skip_to_file=${OPTARG};;
		v) consistency_task=true;;
		c) classification_task=true;;
		r) realization_task=true;;
    esac
done

skip_to_file_bk="$skip_to_file"


mkdir output
mkdir output/consistency
mkdir output/classification
mkdir output/realization
#for i in `ls -Sr ../../ontologies/*`;

echo "Evaluating reasoner Konclude"


if [ "$realization_task" = true ];
then
	echo
	echo "Evaluating Reasoner Realization"
	echo

	for i in `ls -Sr ${ontoDir}`;
	do
	  
	   
	   if [ ! -z "$skip_to_file" ] && [ "$i" != "$skip_to_file" ]; 
	   then
			continue
	   else
			skip_to_file=""
	   fi
	   
	   echo
	   echo "$i"
	   echo
	   output="$i"
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
		output="${output}"
		echo "$output" >> ./output/realization.csv
		
		reasonerClassification=$(echo "scale=3; $reasonerClassification/$RUN" | bc -l)
		echo "Everage Realization time on: $i is $reasonerClassification"
	done
fi


skip_to_file="$skip_to_file_bk"

if [ "$classification_task" = true ] ;
then
	echo
	echo "Evaluating Reasoner Classification"
	echo

	for i in `ls -Sr ${ontoDir}`;
	do
	   
	   if [ ! -z "$skip_to_file" ] && [ "$i" != "$skip_to_file" ]; 
	   then
			continue
	   else
			skip_to_file=""
	   fi
	   
	   echo
	   echo "$i"
	   echo
	   output="$i"
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
		output="${output}"
		echo "$output" >> ./output/classification.csv
		reasonerClassification=$(echo "scale=3; $reasonerClassification/$RUN" | bc -l)
		echo "Everage classification time on: $i is $reasonerClassification"
	done
fi

skip_to_file="$skip_to_file_bk"

if [ "$consistency" = true ] ;
then
	echo
	echo "Evaluating Reasoner consistency validation"
	echo
	for i in `ls -Sr ${ontoDir}`;
	do
	   if [ ! -z "$skip_to_file" ] && [ "$i" != "$skip_to_file" ]; 
	   then
			continue
	   else
			skip_to_file=""
	   fi
	   
	   echo
	   echo "$i"
	   echo
	   output="$i"
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
		output="${output}"
		echo "$output" >> ./output/consistency.csv
		reasonerConsistencyTime=$(echo "scale=3; $reasonerConsistencyTime/$RUN" | bc -l)
		echo "Everage consistency validation time on: $i is $reasonerConsistencyTime"
	done

fi




