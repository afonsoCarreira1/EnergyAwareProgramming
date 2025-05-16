#!/bin/bash

# Exit if any command fails
#set -e

# Set the main filename
MAIN_TEX="main.tex"
OUTPUT_PDF="main.pdf"

# Run pdflatex (first pass)
pdflatex -interaction=nonstopmode -shell-escape "$MAIN_TEX"

# Run bibtex if you're using a .bib file (optional)
#if grep -q "\\bibliography{" "$MAIN_TEX"; then
    echo "Running bibtex..."
    BIBFILE=$(basename "$MAIN_TEX" .tex)
    bibtex "$BIBFILE"
#fi

# Run pdflatex two more times to resolve references
pdflatex -interaction=nonstopmode -shell-escape "$MAIN_TEX"
pdflatex -interaction=nonstopmode -shell-escape "$MAIN_TEX"

echo "cleaning auxiliary files"
rm *.aux
rm main.glo main.idx main.lof main.lot main.log main.out main.ist main.toc main.bbl main.blg
cd tex
rm *.aux

echo "Compilation finished. Output: $OUTPUT_PDF"