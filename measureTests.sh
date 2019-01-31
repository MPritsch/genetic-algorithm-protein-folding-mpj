#!/bin/bash

ga_alg() {
    N=$1
    PROCESSES=$2
    GENERATIONS=$3

    echo -e "\n\n\n----------------------- NEXT ------------------------\n\n\n"
    echo -e "Using $N nodes with $PROCESSES processes calculating $GENERATIONS generations per process\n"
    echo -e "\nRun 1\n\n"
    salloc -N ${N} -n ${PROCESSES} mpjrun.sh -np ${PROCESSES} -dev niodev \
        -jar ~/java-mpj/genetic-algorithm-protein-folding-mpj-1.0-SNAPSHOT-jar-with-dependencies.jar --generation ${GENERATIONS}
    echo -e "\nRun 2\n\n"
    salloc -N ${N} -n ${PROCESSES} mpjrun.sh -np ${PROCESSES} -dev niodev \
        -jar ~/java-mpj/genetic-algorithm-protein-folding-mpj-1.0-SNAPSHOT-jar-with-dependencies.jar --generation ${GENERATIONS}
    echo -e "\nRun 3\n\n"
    salloc -N ${N} -n ${PROCESSES} mpjrun.sh -np ${PROCESSES} -dev niodev \
        -jar ~/java-mpj/genetic-algorithm-protein-folding-mpj-1.0-SNAPSHOT-jar-with-dependencies.jar --generation ${GENERATIONS}
}

runtests() {
    echo -e "\n\n\n##################### NEW TEST SUITE ##########################\n\n\n"

    CORE_COUNT_ARRAY=$1
    N=$2

    echo -e "Using $CORE_COUNT_ARRAY cores on $N hosts"

    for i in "${CORE_COUNT_ARRAY[@]}"
    do
        ga_alg $N $i 10
        ga_alg $N $i 20
        ga_alg $N $i 40
        ga_alg $N $i 80
        ga_alg $N $i 160
    done
}

echo -e "time in ms;mpj process amount;total generation count;total calculated individuals;time limit;generation limit;given commands;\n" >> results.csv

#UNTIL_28=( 1 2 4 8 16 28 )
UNTIL_2=( 1 2 )
UNTIL_56=( 4 8 16 28 32 56 )
UNTIL_128=( 64 128 )
UNTIL_256=( 256 )

runtests $UNTIL_2 2
runtests $UNTIL_56 4
runtests $UNTIL_128 4
runtests $UNTIL_256 8