package org.hda.gaf;

import com.google.common.base.Stopwatch;
import mpi.MPI;
import org.hda.gaf.algorithm.Population;
import org.hda.gaf.algorithm.evaluation.direction.RelativeDirection;
import org.hda.gaf.algorithm.examples.Examples;
import org.hda.gaf.algorithm.geneticalgorithm.GenerationLimitedAlgorithm;
import org.hda.gaf.algorithm.geneticalgorithm.GeneticAlgorithm;
import org.hda.gaf.algorithm.selectionalgorithm.FitnessProportionalSelectionAlgorithm;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {

    final static boolean DOCUMENTS_STATISTIC = false;

    final static int GENERATION_AMOUNT = 163;
    final static long TIME_LIMIT = 10000;
    final static int POPULATION_AMOUNT = 2500;
    final static float MUTATION_RATE = 0.02F;
    final static float CROSSOVER_RATE = 0.25F;

    final static int CANDIDATE_AMOUNT_PER_SELECTION = 200;

    final static boolean CALC_HEMMING_DISTANCE = false;

    final static String PRIMARY_SEQUENCE = Examples.SEQ156;

    final static boolean PRINT_WHILE_GENERATING = false;

    public static void main(String[] args) {
        int rank, size;

        MPI.Init(args);
        size = MPI.COMM_WORLD.Size();
        rank = MPI.COMM_WORLD.Rank();

        int nextNeighbor = (rank + 1) % size;
        int prevNeighbor = (rank + size - 1) % size;
        int individualAmountToSend = POPULATION_AMOUNT / 5;

        Stopwatch s = Stopwatch.createStarted();

//        GeneticAlgorithm geneticAlgorithm = new TimeLimitedAlgorithm().usesTimeLimit(TIME_LIMIT);
        GeneticAlgorithm geneticAlgorithm = new GenerationLimitedAlgorithm().usesGenerationLimit(GENERATION_AMOUNT / 5);

        //TODO every x iterations share random results with neighbors
        geneticAlgorithm
                .startedAt(Instant.now().toEpochMilli())
                .documentsStatistic(DOCUMENTS_STATISTIC)
                .usesPrimarySequence(PRIMARY_SEQUENCE)
                .hasPopulationAmountOf(POPULATION_AMOUNT)
                .hasCrossoverRateOf(CROSSOVER_RATE)
                .hasMutationRateOf(MUTATION_RATE)
                .calculatesHammingDistance(CALC_HEMMING_DISTANCE)
                .printsWhileGenerating(PRINT_WHILE_GENERATING)
                .usesSelectionAlgorithm(new FitnessProportionalSelectionAlgorithm())
//                .usesSelectionAlgorithm(new TunierFitnessProportionalSelectionAlgorithm(CANDIDATE_AMOUNT_PER_SELECTION))
//                .usesSelectionAlgorithm(new TunierBestFitnessSelectionAlgorithm(CANDIDATE_AMOUNT_PER_SELECTION))
//                .usesSelectionAlgorithm(new SigmaScalingSelectionAlgorithm())
        ;
        Population population = geneticAlgorithm.generateStartPopulation();

        Population neighborPopulation = new Population(POPULATION_AMOUNT);

        //Every process does this
        for (int i = 0; i < 5; i++) {
            population = geneticAlgorithm.runAlgorithm(population);

            if (rank == 0) {
                sendToNeighbor(nextNeighbor, individualAmountToSend, population);
                receiveFromNeighbor(prevNeighbor, neighborPopulation);
            } else {
                receiveFromNeighbor(prevNeighbor, neighborPopulation);
                sendToNeighbor(nextNeighbor, individualAmountToSend, population);
            }
        }

        //TODO get overall best candidate

        if (rank == 0) {
            System.out.println("Calculation took: " + s.elapsed(TimeUnit.MILLISECONDS));
            population.printStatusOfCurrentGeneration(geneticAlgorithm.getCurrentGeneration(), population.getBestProtein());
            saveChart(population);
        }

        MPI.Finalize();
    }

    private static void sendToNeighbor(int to, int individualAmountToSend, Population population) {
        //TODO send only part to neighbor
        List<List<RelativeDirection>> genepool = population.getGenepool();

        Collections.shuffle(genepool);

        String genePoolInStrings = genepool.stream().limit(individualAmountToSend)
                .map(relativeDirections ->
                        relativeDirections.stream()
                                .map(RelativeDirection::getCharacter)
                                .map(Object::toString)
                                .collect(Collectors.joining()))
                .collect(Collectors.joining());

        String[] send = new String[1];
        send[0] = genePoolInStrings;

        MPI.COMM_WORLD.Send(send, 0, PRIMARY_SEQUENCE.length() * individualAmountToSend, MPI.OBJECT, to, 0);
    }

    private static void receiveFromNeighbor(int from, Population population) {
        String[] receive = new String[1];
        MPI.COMM_WORLD.Send(receive, 0, 1, MPI.OBJECT, from, 0);

        int singleSequenceSize = PRIMARY_SEQUENCE.length() - 1;
        String singleSequenceRegex = "(?=\\G.{" + singleSequenceSize + "})";
        String[] splittedGenepool = receive[0].split(singleSequenceRegex);

        List<List<RelativeDirection>> receivedGenepool = Arrays.stream(splittedGenepool)
                .map(singleGenom ->
                        singleGenom.chars()
                                .mapToObj(i -> (char) i)
                                .map(RelativeDirection::fromChar)
                                .collect(Collectors.toList()))
                .collect(Collectors.toList());


        List<List<RelativeDirection>> genepool = population.getGenepool();
        Collections.shuffle(genepool);
        for (List<RelativeDirection> genom : receivedGenepool) {
            genepool.remove(0);
            genepool.add(genom);
        }
    }

    private static void saveChart(Population population) {
        DefaultCategoryDataset lineChartDataset = population.getLineChartDataset();

        JFreeChart lineChartObject = ChartFactory.createLineChart(
                "Genetic Algorithm flow", "Generation",
                "Fitness",
                lineChartDataset, PlotOrientation.VERTICAL,
                true, true, false);

        File lineChart = new File("GeneticAlgorithm.jpeg");

        try {
            ChartUtilities.saveChartAsJPEG(lineChart, lineChartObject, 1280, 720);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
