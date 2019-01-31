package org.hda.gaf.algorithm;

import org.hda.gaf.algorithm.geneticalgorithm.GenerationLimitedAlgorithm;
import org.hda.gaf.algorithm.geneticalgorithm.GeneticAlgorithm;
import org.hda.gaf.algorithm.geneticalgorithm.TimeLimitedAlgorithm;
import org.hda.gaf.algorithm.selectionalgorithm.FitnessProportionalSelectionAlgorithm;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.hda.gaf.DefaultOptions.*;

public class GeneticAlgorithmGenerator {

    public GeneticAlgorithm fromArgs(String[] args, int rank) {
        printCommandLineOptions(rank);

        GeneticAlgorithm geneticAlgorithm;

        geneticAlgorithm = getBaseAlgorithmFromArgs(args, rank);

        geneticAlgorithm
                .startedAt(Instant.now().toEpochMilli())
                .documentsStatistic(DOCUMENTS_STATISTIC)
                .usesPrimarySequence(PRIMARY_SEQUENCE)
                .hasPopulationAmountOf(POPULATION_AMOUNT)
                .hasCrossoverRateOf(CROSSOVER_RATE)
                .hasMutationRateOf(MUTATION_RATE)
                .usesSelectionAlgorithm(new FitnessProportionalSelectionAlgorithm())
                .calculatesHammingDistance(CALC_HEMMING_DISTANCE)
                .printsWhileGenerating(PRINT_WHILE_GENERATING);

        return geneticAlgorithm;
    }

    private GeneticAlgorithm getBaseAlgorithmFromArgs(String[] args, int rank) {
        GeneticAlgorithm geneticAlgorithm;
        List<String> commands = Arrays.asList(args);
        int timeCommandIndex = commands.indexOf("--time");
        int generationCommandIndex = commands.indexOf("--generation");

        if (timeCommandIndex != -1) {

            geneticAlgorithm = getTimeLimitedAlgorithm(args, rank, commands, timeCommandIndex);

        } else if (generationCommandIndex != -1) {
            geneticAlgorithm = getGenerationLimitedAlgorithm(args, rank, commands, generationCommandIndex);

        } else {
            geneticAlgorithm = getDefaultAlgorithm(rank);
        }
        return geneticAlgorithm;
    }

    private GeneticAlgorithm getDefaultAlgorithm(int rank) {
        GeneticAlgorithm geneticAlgorithm;
        geneticAlgorithm = new GenerationLimitedAlgorithm().usesGenerationLimit(GENERATION_AMOUNT / MPJ_POPULATION_EXCHANGE_TIMES);
        if (rank == 0) {
            System.out.println("Executing default 'generation' limited algorithm with total " + GENERATION_AMOUNT + " per process.");
        }
        return geneticAlgorithm;
    }

    private GeneticAlgorithm getTimeLimitedAlgorithm(String[] args, int rank, List<String> commands, int timeCommandIndex) {
        GeneticAlgorithm geneticAlgorithm;
        long timeLimit = TIME_LIMIT;

        if (args.length > timeCommandIndex) {
            timeLimit = Long.valueOf(commands.get(timeCommandIndex + 1));
        }

        geneticAlgorithm = new TimeLimitedAlgorithm().usesTimeLimit(timeLimit / MPJ_POPULATION_EXCHANGE_TIMES);
        if (rank == 0) {
            System.out.println("Executing 'time' limited algorithm with " + timeLimit + " ms per process.");
        }
        return geneticAlgorithm;
    }

    private GeneticAlgorithm getGenerationLimitedAlgorithm(String[] args, int rank, List<String> commands, int generationCommandIndex) {
        GeneticAlgorithm geneticAlgorithm;
        int generationAmount = GENERATION_AMOUNT;

        if (args.length > generationCommandIndex) {
            generationAmount = Integer.valueOf(commands.get(generationCommandIndex + 1));
        }

        geneticAlgorithm = new GenerationLimitedAlgorithm().usesGenerationLimit(generationAmount / MPJ_POPULATION_EXCHANGE_TIMES);
        if (rank == 0) {
            System.out.println("Executing 'generation' limited algorithm with total " + generationAmount + " per process.");
        }
        return geneticAlgorithm;
    }

    private static void printCommandLineOptions(int rank) {
        if (rank == 0) {
            System.out.println("Following command line options available:\n" +
                    "# use default options 'generation' with " + GENERATION_AMOUNT + " generations\n" +
                    "mpjrun.sh -np 4 -jar <jar-path>\n \n" +
                    "# specify total time spend per process\n" +
                    "mpjrun.sh -np 4 -jar <jar-path> --time <time in ms>\n \n" +
                    "# specify total generation amount per process\n" +
                    "mpjrun.sh -np 4 -jar <jar-path> --generation <generations>\n \n");
            System.out.flush();
        }
    }
}
