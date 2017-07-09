#!/bin/bash

gwas_jar=/home/clados/www/repGenome/java/GwasRisk/bin/dist/GwasRisk-1.0-SNAPSHOT.jar
input_dir=/home/clados/bioinformaticsData/trios/MS/IndependentlyPhased/Training

#java -jar $gwas_jar imputeRef --input=$input_dir  --output=$input_dir/haps_casecont   --mode=casecont
#java -jar $gwas_jar imputeRef --input=$input_dir  --output=$input_dir/haps_control   --mode=control
java -jar $gwas_jar imputeRef --input=$input_dir  --output=$input_dir/haps_parents
