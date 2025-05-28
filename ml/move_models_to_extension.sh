#!/bin/bash

cd out/
output_dir="collected_models"
mkdir -p "$output_dir"

# Initialize the models list file
models_list_file="$output_dir/ModelsAvailable.txt"
> "$models_list_file"  # Truncate or create the file

# Loop over all directories in the current directory
for dir in */ ; do
    # Remove trailing slash
    dir=${dir%/}
    
    # Construct the source file path
    src_file="$dir/models/pysr/$dir/models/hall_of_fame.csv"
    
    # Check if the source file exists
    if [[ -f "$src_file" ]]; then
        cp "$src_file" "$output_dir/$dir.csv"
        echo "Copied $src_file to $output_dir/$dir.csv"

        # Add the model name (without .csv) to the list
        echo "$dir" >> "$models_list_file"
    else
        echo "File not found: $src_file"
    fi
done


rm -rf ../../ext/server/ModelsAvailable.txt
rm -rf ../../ext/server/collected_models/*
mv collected_models/ModelsAvailable.txt ../../ext/server/
mv collected_models/ ../../ext/server/

