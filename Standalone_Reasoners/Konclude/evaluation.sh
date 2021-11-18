#!/bin/bash

NEWLINE=$'\n'
ontoDir="../../ontologies/ontologies/"
outDir="output"
RUN=10
#rm -rf ./output/

consistency_task=false
classification_task=false
realization_task=false
skip_to_file=""
min="30m"

while getopts m:o:i:j:vcr flag
do
    case "${flag}" in
		m) min=${OPTARG};;
		o) outDir=${OPTARG};;
		i) ontoDir=${OPTARG};;
        j) skip_to_file=${OPTARG};;
		v) consistency_task=true;;
		c) classification_task=true;;
		r) realization_task=true;;
    esac
done

skip_to_file_bk="$skip_to_file"


mkdir -p "$outDir"
mkdir -p "${outDir}/consistency"
mkdir -p "${outDir}/classification"
mkdir -p "${outDir}/realization"
#for i in `ls -Sr ../../ontologies/*`;

echo "Evaluating reasoner Konclude"

if [ "$consistency_task" = true ] ;
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
	   consistencyResult="$i"
	   reasonerConsistencyTime=0
	   for (( runC=1; runC<=$RUN; runC++ )) 
	   do 
			
			timeout $min start=$(date +%s.%3N) && Konclude consistency -i "$ontoDir$i" -o "./${outDir}/consistency/consistency_${i}" > "./${outDir}/consistency_${i}.log" && runtime=$( echo "scale=3; $end - $start" | bc -l )
			
			EXIT_STATUS=$?
			if [ $EXIT_STATUS -eq 124 ]
			then
				echo 'Process Timed Out!'
				killall  Konclude
				runtime="Timeout"
				output="${output},${runtime}"
				break
			fi
			
			
			output="${output},${runtime}"
			
			result=$(cat "./${outDir}/consistency/consistency_${i}")
			consistencyResult="${consistencyResult},${result}"
			
			reasonerConsistencyTime=$(echo "scale=3; $runtime + $reasonerConsistencyTime" | bc -l)
			echo "Consistency validation takes ${runtime}"
		done
		output="${output}"
		consistencyResult="${consistencyResult}"
		
		echo "$output" >> "./${outDir}/KoncludeCLI_Consistency.csv"
		echo "$consistencyResult" >> "./${outDir}/KoncludeCLI_ConsistencyResult.csv"
		reasonerConsistencyTime=$(echo "scale=3; $reasonerConsistencyTime/$RUN" | bc -l)
		echo "Everage consistency validation time on: $i is $reasonerConsistencyTime"
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
			Konclude classification -i "$ontoDir$i" -o "./${outDir}/classification/classification_${i}" > "./${outDir}/classification_${i}.log"
			end=$(date +%s.%3N)
			runtime=$( echo "scale=3; $end - $start" | bc -l )
			output="${output},${runtime}"
			reasonerClassification=$(echo "scale=3; $runtime + $reasonerClassification" | bc -l)
			echo "Classification takes ${runtime}"
		done
		output="${output}"
		echo "$output" >> "./${outDir}/KoncludeCLI_Classification.csv"
		reasonerClassification=$(echo "scale=3; $reasonerClassification/$RUN" | bc -l)
		echo "Everage classification time on: $i is $reasonerClassification"
	done
fi


skip_to_file="$skip_to_file_bk"

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
			Konclude realization -i "$ontoDir$i" -o "./${outDir}/realization/realization_${i}" > "./${outDir}/realization_${i}.log"
			end=$(date +%s.%3N)
			runtime=$( echo "scale=3; $end - $start" | bc -l )
			output="${output},${runtime}"
			reasonerClassification=$(echo "scale=3; $runtime + $reasonerClassification" | bc -l)
			echo "Realization takes ${runtime}"
		done
		output="${output}"
		echo "$output" >> "./${outDir}/KoncludeCLI_Realization.csv"
		
		reasonerClassification=$(echo "scale=3; $reasonerClassification/$RUN" | bc -l)
		echo "Everage Realization time on: $i is $reasonerClassification"
	done
fi










