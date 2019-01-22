package org.hda.gaf;

import org.hda.gaf.algorithm.examples.Examples;

public class DefaultOptions {

    public final static boolean DOCUMENTS_STATISTIC = false;

    public final static int GENERATION_AMOUNT = 163;
    public final static long TIME_LIMIT = 10000;
    public final static int POPULATION_AMOUNT = 2500;
    public final static float MUTATION_RATE = 0.02F;
    public final static float CROSSOVER_RATE = 0.25F;

    public final static int CANDIDATE_AMOUNT_PER_SELECTION = 200;

    public final static boolean CALC_HEMMING_DISTANCE = false;

    public final static String PRIMARY_SEQUENCE = Examples.SEQ156;

    public final static boolean PRINT_WHILE_GENERATING = false;

    public static final int MPJ_POPULATION_EXCHANGE_TIMES = 5;
}
