#!/bin/bash
#SBATCH --job-name=codegen
#SBATCH --output=codegen_%A_%a.log
#SBATCH --array=0-0
#SBATCH --ntasks=1
#SBATCH --mem=1G

#test args
ARGS=(
    "lists addAll,containsAll"
)

./run_codegen.sh ${ARGS[$SLURM_ARRAY_TASK_ID]}
