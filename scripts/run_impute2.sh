#!/bin/bash

gwas_jar=/home/clados/victor/GwasRisk-1.0-SNAPSHOT-bin/dist/GwasRisk-1.0-SNAPSHOT.jar
gens_dir=/home/clados/bioinformaticsData/WTCCC2/MS/chiamo
out_dir=/home/clados/victor/phasing_control
impute_dir=/home/clados/bioinformaticsSoftware/impute_v2.3.0_x86_64_static
hapmap3_r2=/home/clados/bioinformaticsData/hapmap3_r2_b36
haps_control_dir=/home/clados/bioinformaticsData/trios/MS/IndependentlyPhased/Training/haps_control
haps_casecont_dir=/home/clados/bioinformaticsData/trios/MS/IndependentlyPhased/Training/haps_casecont

echo java -jar $gwas_jar imputePhasing --input=$gens_dir --output=$out_dir --impute=$impute_dir --haps=$haps_control_dir --legend=$haps_control_dir --map=$hapmap3_r2
