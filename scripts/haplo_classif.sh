#!/bin/bash
#
#PBS –N haploArffClas_
#PBS –M vpoten@gmail.com
#PBS -m abe
#PBS -l mem=16gb

#diseases=("BD" "CAD" "HT" "IBD" "RA" "T1D" "T2D")
gmodels=("recessive" "additive" "dominant")
disease=$1
winSizes=("1"  "2"  "3"  "4"  "5")
gwas_jar=$PBS_O_HOME/GwasRisk/bin/dist/GwasRisk-1.0-SNAPSHOT.jar
input_dir=$PBS_O_HOME/
out_dir=$PBS_O_HOME/

for i in "${winSizes[@]}"
do
    for j in "${gmodels[@]}"
    do
	echo "Classify size " $i ", gmodel " $j
	echo java -Xmx16g -jar $gwas_jar haploArffClas --input=$input_dir/$disease --output=$out_dir --size=$i --mode=$j  > $out_dir/$disease"_"$i"_"$j.result
    done
done