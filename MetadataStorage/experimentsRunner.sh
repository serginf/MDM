#!/bin/sh
for i in `seq 2 7`
do
	for j in `seq 2 25`
	do
		timeout 10m java -jar build/libs/MetadataStorage-0.0.1-SNAPSHOT.jar 50 5 $i $j 0.15 0.75
                timeout 10m java -jar build/libs/MetadataStorage-0.0.1-SNAPSHOT.jar 50 5 $i $j 0.15 0.75
                timeout 10m java -jar build/libs/MetadataStorage-0.0.1-SNAPSHOT.jar 50 5 $i $j 0.15 0.75
	done
done
