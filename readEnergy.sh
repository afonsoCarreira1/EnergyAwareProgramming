#!/bin/bash
#SBATCH --job-name=orchestrator
#SBATCH --output=log_orchestrator_%A_%a.log
#SBATCH --array=0
#SBATCH --ntasks=1
#SBATCH --mem=2G

./run_orchestrator.sh