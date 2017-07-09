#!/bin/bash

jars=/home/victor/NetBeansProjects/GwasRisk/bin/dist/GwasRisk-1.0-SNAPSHOT.jar
script=generate_shapeitref.groovy
input=/home/victor/Escritorio/WTCCC1/trios/test
output=/home/victor/Escritorio/WTCCC1/out_phasing

groovy -cp $jars $script $input $output
