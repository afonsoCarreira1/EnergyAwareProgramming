#!/bin/bash
#SBATCH --job-name=codegen
#SBATCH --output=log_codegen_%A_%a.log
#SBATCH --array=0-0
#SBATCH --ntasks=1
#SBATCH --mem=1G

#test args
ARGS=(
    "lists addAll",
    "lists containsAll"
) 

./run_codegen.sh ${ARGS[$SLURM_ARRAY_TASK_ID]}
