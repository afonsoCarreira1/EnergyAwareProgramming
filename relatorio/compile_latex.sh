#!/bin/bash

# Name of your main LaTeX file (without extension)
MAIN_TEX="main"

# Run pdflatex to create the initial .aux file and process citations, sending output to /dev/null
pdflatex "$MAIN_TEX.tex" > /dev/null 2>&1

# Run BibTeX to process the .bib file and generate the .bbl file, sending output to /dev/null
bibtex "$MAIN_TEX" > /dev/null 2>&1

# Run pdflatex twice more to resolve all references, sending output to /dev/null
pdflatex "$MAIN_TEX.tex" > /dev/null 2>&1
pdflatex "$MAIN_TEX.tex" > /dev/null 2>&1

# Optional: clean up auxiliary files
echo "Cleaning up auxiliary files..."
rm -f "$MAIN_TEX.aux" "$MAIN_TEX.bbl" "$MAIN_TEX.blg" "$MAIN_TEX.log" "$MAIN_TEX.out"

echo "Compilation complete! Check $MAIN_TEX.pdf for the final document."