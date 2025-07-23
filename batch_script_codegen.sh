#!/bin/bash
#SBATCH --job-name=codegen
#SBATCH --output=log_codegen_%A_%a.log
#SBATCH --array=0-1
#SBATCH --ntasks=1
#SBATCH --mem=1G

#test args
ARGS=(
    #"spectralnorm Approximate",
    #"spectralnorm A",
    #"spectralnorm MultiplyAv",
    #"spectralnorm MultiplyAtv",
    #"spectralnorm MultiplyAtAv",
    #"fannkuchredux fannkuch",
    "NBodySystem advance",
    #"NBodySystem energy"
) 

ARG_PAIR="${ARGS[$SLURM_ARRAY_TASK_ID]}"
targetProgram=$(echo "$ARG_PAIR" | awk '{print $1}')
targetMethods=$(echo "$ARG_PAIR" | cut -d' ' -f2- | sed -E 's/\s*,\s*/,/g' | tr -d ' ')


cd codegen/  
echo "[Task $SLURM_ARRAY_TASK_ID] Running codegen for $targetProgram $targetMethods"
java -jar target/codegen-1.0-SNAPSHOT-jar-with-dependencies.jar $targetProgram $targetMethods


#"lib_java.lang.Math abs",
#"lib_java.lang.Math max",
#"lib_java.lang.Math min",
#"lib_java.lang.Math pow",
#"lib_java.lang.Math sqrt",
#"lib_java.lang.Math round",
#"lib_java.lang.Math floor",
#"lib_java.lang.Math ceil",
#"lib_java.lang.Math random",
#"lib_java.lang.Math sin",
#"lib_java.lang.Math cos",
#"lib_java.lang.Math tan",
#"lib_java.lang.Math log",
#"lib_java.lang.Math log10",
#"lib_java.lang.Math exp",
#"lib_java.lang.Math toRadians",
#"lib_java.lang.Math toDegrees",
#"lib_java.lang.Math hypot",
#"lib_java.lang.Math ulp",
#"lib_java.lang.Math copySign"


#ARGS=(
#    "BinaryTrees checkTree",
#    "BinaryTrees createTree",
#    "BinaryTrees trees",
#    "BinaryTrees loops"
#) 