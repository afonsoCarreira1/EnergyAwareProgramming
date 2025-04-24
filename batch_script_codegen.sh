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

./run_codegen.sh ${ARGS[$SLURM_ARRAY_TASK_ID]}
