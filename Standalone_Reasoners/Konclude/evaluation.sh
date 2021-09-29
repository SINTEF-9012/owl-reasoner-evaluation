#!/bin/bash

echo "Evaluating reasoner Konclude"

echo "Evaluating Reasoner consistency validation"
start=$(date +%s.%N)
echo "Evaluating reasoner Konclude"
end=$(date +%s.%N)

runtime=$( echo "$end - $start" | bc -l )

echo "Evaluating Reasoner Classification"



cmd /k