#!/bin/bash

NEWLINE=$'\n'
ontoDir="../../ontologies/ontologies/"
outDir="output"
RUN=10
#rm -rf ./output/
Konclude="/home/ubuntu/SkyTrack/Standalone_Reasoners/Konclude/Konclude-Linux/Binaries/Konclude"

consistency_task=false
classification_task=false
realization_task=true
skip_to_file=""
listFile=""
min="30m"

while getopts m:o:i:j:l: flag
do
    case "${flag}" in
		m) min=${OPTARG};;
		o) outDir=${OPTARG};;
		i) ontoDir=${OPTARG};;
        j) skip_to_file=${OPTARG};;
		l) listFile=${OPTARG};;
    esac
done

skip_to_file_bk="$skip_to_file"


mkdir -p "$outDir"
mkdir -p "${outDir}/realization"
#for i in `ls -Sr ../../ontologies/*`;

filesToEvaluate=""
if [ ! -z "$listFile" ]; 
then
	filesToEvaluate=$(cat "$listFile")
fi


	   
	   
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
	   

	   	if [[ $filesToEvaluate != *"$i"* ]]; then
			continue
		fi
		
			   
	   echo
	   echo "$i"
	   echo
		
	   output="$i"
	   reasonerClassification=0
	   for (( runC=1; runC<=$RUN; runC++ )) 
	   do 
			start=$(date +%s.%3N)
			timeout $min $Konclude realization -i "$ontoDir$i" -o "./${outDir}/realization/realization_${i}" > "./${outDir}/realization_${i}.log"
			EXIT_STATUS=$?
			end=$(date +%s.%3N)
			
			if [ $EXIT_STATUS -eq 124 ]
			then
				echo 'Process Timed Out!'
				killall  Konclude
				runtime="Timeout"
				output="${i},${runtime}"
				break
			fi
			
			
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










