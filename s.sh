#!/bin/bash

./create_templates.sh
./run.sh j test

rm -rf java_progs/templates/*
cp java_progs/templates_old/TemplateGetCol java_progs/templates

#./create_templates.sh
./run.sh j test

sleep 10
sudo systemctl poweroff
