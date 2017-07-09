#!/bin/bash

#"1"  "2"  "5"  "20"  "50"  "100"  "150"
winSizes=("1"  "2"  "5"  "20"  "50")
gwas_jar=/home/victor/GwasRisk-1.0-SNAPSHOT-bin/dist/GwasRisk-1.0-SNAPSHOT.jar
input_dir=/home/victor/trios

for i in "${winSizes[@]}"
do
	echo "Generating size" $i
	mkdir "size_"$i
	java -Xmx15g -jar $gwas_jar triosClas --input=$input_dir/ --output="size_"$i --size=$i  > $i.result
	echo "Generating size" $i "2gtree"
	mkdir "size2gtree_"$i
	java -Xmx15g -jar $gwas_jar triosClas --input=$input_dir/ --output="size2gtree_"$i --size=$i --2gtree > $i"_2gtree.result"
done
