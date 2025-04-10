#!/bin/bash
#SBATCH --job-name=codegen
#SBATCH --output=codegen_%A_%a.log
#SBATCH --array=0-0
#SBATCH --ntasks=1
#SBATCH --mem=1G

#test args
ARGS=(
    "fibonacci"
)

echo ./run_codegen ${ARGS[$SLURM_ARRAY_TASK_ID]}
