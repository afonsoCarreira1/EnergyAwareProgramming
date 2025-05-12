#!/bin/bash
#SBATCH --job-name=codegen
#SBATCH --output=log_codegen_%A_%a.log
#SBATCH --array=0-17
#SBATCH --ntasks=1
#SBATCH --mem=1G

#test args
ARGS=(
    "sets add",
    "sets remove",
    "sets contains",
    "sets isEmpty",
    "sets size",
    "sets clear",
    "sets iterator",
    "maps put",
    "maps get",
    "maps removes",
    "maps containsKey",
    "maps containsValue",
    "maps isEmpty",
    "maps size",
    "maps clear",
    "maps keySet",
    "maps values",
    "maps entrySet",
) 

ARG_PAIR="${ARGS[$SLURM_ARRAY_TASK_ID]}"
targetProgram=$(echo "$ARG_PAIR" | awk '{print $1}')
targetMethods=$(echo "$ARG_PAIR" | cut -d' ' -f2- | sed -E 's/\s*,\s*/,/g' | tr -d ' ')


cd codegen/  
echo "[Task $SLURM_ARRAY_TASK_ID] Running codegen for $targetProgram $targetMethods"
java -jar target/codegen-1.0-SNAPSHOT-jar-with-dependencies.jar $targetProgram $targetMethods
