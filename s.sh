#!/bin/bash

./create_templates.sh
./run.sh j test

rm -rf java_progs/templates/*
cp java_progs/templates_old/TemplateSizeCol java_progs/templates

./create_templates.sh
./run.sh j test