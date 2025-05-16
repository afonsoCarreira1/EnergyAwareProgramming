#!/bin/bash

tmux new -s orchestrator './run_orchestrator.sh'

#leave session 
# Ctrl + b, then d

# reattach to session
# tmux attach -t orchestrator