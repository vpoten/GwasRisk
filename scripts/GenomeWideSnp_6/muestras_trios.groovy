#!/usr/bin/env groovy

/**
 *       Generates a tfam template:
 *
 *	 Family ID
 *       Individual ID
 *       Paternal ID
 *       Maternal ID
 *       Sex (1=male; 2=female; other=unknown)
 *       Phenotype (0=missing; 1=unaffected; 2=affected)
 **/

/***
println "Family_ID,Individual_ID,Paternal_ID,Maternal_ID,Sex,Phenotype"

(1..104).each{
	println "${it},${'h_id'},${'p_id'},${'m_id'},${0},${2}" //hijo
	println "${it},${'m_id'},${0},${0},${2},${1}" //madre
	println "${it},${'p_id'},${0},${0},${1},${1}" //padre
}
***/

def input = this.args[0]

def reader = new File(input).newReader()

reader.readLine()//skip Header

int cont = 0
def lines = [[],[],[]]


reader.splitEachLine(","){ toks->
	lines[(cont%3)]=toks
	cont++

	if(cont%3==0){
		println "${lines[0][0]},${lines[0][1]},${lines[2][1]},${lines[1][1]},${lines[0][4]},${lines[0][5]}"
		(1..2).each{ println (lines[it].sum{','+it}.substring(1)) }
	}
}

reader.close()
