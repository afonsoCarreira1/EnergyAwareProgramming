#!/bin/bash
#SBATCH --job-name=codegen
#SBATCH --output=log_codegen_%A_%a.log
#SBATCH --array=0-9
#SBATCH --ntasks=1
#SBATCH --mem=1G

#test args
ARGS=(
    "lists addAll",
    "lists containsAll",
    "lists add",
    "lists get",
    "lists equals",
    "lists replaceAll",
    "lists retainAll",
    "lists set",
    "lists subList",
    "lists size"
) 

ARG_PAIR="${ARGS[$SLURM_ARRAY_TASK_ID]}"
targetProgram=$(echo "$ARG_PAIR" | awk '{print $1}')
targetMethods=$(echo "$ARG_PAIR" | cut -d' ' -f2- | sed -E 's/\s*,\s*/,/g' | tr -d ' ')


cd codegen/  
echo "[Task $SLURM_ARRAY_TASK_ID] Running codegen for $targetProgram $targetMethods"
java -jar target/codegen-1.0-SNAPSHOT-jar-with-dependencies.jar $targetProgram $targetMethods
