#!/bin/bash

ga_alg() {
    PROCESSES=$1
    GENERATIONS=$2

    echo -e "\n\n\n----------------------- NEXT ------------------------\n\n\n"
    echo -e "Using any amount of nodes with $PROCESSES processes calculating $GENERATIONS generations per process\n"
    echo -e "\nRun 1\n\n"
    salloc -n ${PROCESSES} mpjrun.sh -np ${PROCESSES} -dev niodev \
        -jar ~/java-mpj/genetic-algorithm-protein-folding-mpj-1.0-SNAPSHOT-jar-with-dependencies.jar --generation ${GENERATIONS}
    echo -e "\nRun 2\n\n"
    salloc -n ${PROCESSES} mpjrun.sh -np ${PROCESSES} -dev niodev \
        -jar ~/java-mpj/genetic-algorithm-protein-folding-mpj-1.0-SNAPSHOT-jar-with-dependencies.jar --generation ${GENERATIONS}
    echo -e "\nRun 3\n\n"
    salloc -n ${PROCESSES} mpjrun.sh -np ${PROCESSES} -dev niodev \
        -jar ~/java-mpj/genetic-algorithm-protein-folding-mpj-1.0-SNAPSHOT-jar-with-dependencies.jar --generation ${GENERATIONS}
}

runtests() {
    echo -e "\n\n\n##################### NEW TEST SUITE ##########################\n\n\n"

    CORE_COUNT_ARRAY=($@)

    echo -e "Using $CORE_COUNT_ARRAY cores"

    ga_alg 256 10
    ga_alg 256 20
    ga_alg 256 40
    ga_alg 256 80
    ga_alg 256 160
}

echo -e "time in ms;mpj process amount;total generation count;total calculated individuals;time limit;generation limit;given commands;\n" >> results.csv

runtests