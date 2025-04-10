#!/bin/bash
#SBATCH --job-name=codegen
#SBATCH --output=codegen_%A_%a.log
#SBATCH --array=0-1
#SBATCH --ntasks=1
#SBATCH --time=00:01:00
#SBATCH --mem=1G

#test args
ARGS=(
    "fibonacci"
    "lists get"
)

echo ./run_codegen ${ARGS[$SLURM_ARRAY_TASK_ID]}
