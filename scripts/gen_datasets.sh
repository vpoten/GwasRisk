#!/bin/bash

if [ $# -lt 2 ]
then
echo You must introduce whether to use cross-validation or not and the name of folder containing input files -tplink_format/plink_format/tplink_format_QC/plink_format_QC/ depending on the input format -tplink/plink and whether QC has been already passed or not -/QC-. You may also introduce the maximum RAM to be used, default is 8GB
exit 0
fi

test="10foldSimul"
cv=$1
if [ $cv == 0 ]
then
test="trainingTest"
fi 
p=1
format=$2
mem=8
if [ $# -eq 3 ]
then
mem=$3
fi

#"BD"  "CAD"  "HT"  "IBD"  "NBS"  "RA"  "T1D"  "T2D"
diseases=("BD" "CAD" "HT" "IBD" "RA" "T1D" "T2D")
plink=/home/clados/bioinformaticsSoftware/plink/plink
gwas_jar=/home/clados/repGenome/java/GwasRisk/bin/dist/GwasRisk-1.0-SNAPSHOT.jar
input_dir=/home/clados/bioinformaticsData/WTCCC1
cont=1
for i in "${diseases[@]}"
do
	echo "Generating" $i
	if [ ! -d $i ]
	then
	mkdir $i
	fi
	java -Xmx$mem"g" -jar $gwas_jar $test --input=$input_dir/$i/$format/ --control=$input_dir/58C/$format/ --output=$i --plink=$plink   > $i.result &

if [ $(($cont%$p == 0 )) ]
then
wait
fi
cont=$[$cont+1]
#exit 0
done

