#!/bin/bash

script=/home/victor/GwasRisk-1.0-SNAPSHOT-bin/shapeithaps_to_gou.groovy
haps_dir=/home/victor/MSAndMSES2013phasedFrommsCEUFebruary2009
ref_dir=/home/clados/bioinformaticsData/trios/MS/IndependentlyPhased/Training/haps_parents
out_dir=/home/victor/MSAndMSES2013phasedFrommsCEUFebruary2009

groovy $script $haps_dir $ref_dir $out_dir
