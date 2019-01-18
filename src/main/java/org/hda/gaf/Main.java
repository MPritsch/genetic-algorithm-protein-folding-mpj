package org.hda.gaf;

import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import mpi.MPI;
import org.hda.gaf.algorithm.Population;
import org.hda.gaf.algorithm.evaluation.direction.RelativeDirection;
import org.hda.gaf.algorithm.evaluation.node.Structure;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Main {

    final static boolean DOCUMENTS_STATISTIC = false;

    final static int GENERATION_AMOUNT = 100;
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

        //Every process does this
        for (int i = 0; i < 5; i++) {
            population = geneticAlgorithm.runAlgorithm(population);

            List<List<RelativeDirection>> neighborGenepool = shareAndReceiveNeighborPool(rank, nextNeighbor, prevNeighbor, individualAmountToSend, population);

            addAndReplaceNeighborGenes(population, neighborGenepool);
        }

        Population bestPopulation = collectPopulationWithBestProteins(rank, size, population);

        if (rank == 0) {
            System.out.println("Current generation without size: " + geneticAlgorithm.getCurrentGeneration());
            System.out.println("Size: " + size);
            int calculatedGenerations = geneticAlgorithm.getCurrentGeneration() * size;
            int calculatedPopulations = POPULATION_AMOUNT * calculatedGenerations;
            System.out.println("Calculation took: " + s.elapsed(TimeUnit.MILLISECONDS) + " ms");
            System.out.println("Calculation generations: " + calculatedGenerations);
            System.out.println("Calculation populations: " + calculatedPopulations);


            bestPopulation.printStatusOfCurrentGeneration(calculatedGenerations, bestPopulation.getBestProtein());
            saveChart(population);
        }

        MPI.Finalize();
    }

    private static List<List<RelativeDirection>> shareAndReceiveNeighborPool(int rank, int nextNeighbor, int prevNeighbor, int individualAmountToSend, Population population) {
        List<List<RelativeDirection>> neighborGenepool;

        if (rank == 0) {
            sendPoolToNeighbor(nextNeighbor, individualAmountToSend, population);
            neighborGenepool = receivePoolFromNeighbor(prevNeighbor);
        } else {
            neighborGenepool = receivePoolFromNeighbor(prevNeighbor);
            sendPoolToNeighbor(nextNeighbor, individualAmountToSend, population);
        }
        return neighborGenepool;
    }

    private static Population collectPopulationWithBestProteins(int rank, int size, Population population) {
        if (rank == 0) {
            Population bestPopulation = new Population(size);
            bestPopulation.addGensToGenpool(population.getBestProtein().getRelativeDirections());

            for (int from = 1; from < size; from++) {
                String[] receive = new String[1];

                MPI.COMM_WORLD.Recv(receive, 0, 1, MPI.OBJECT, from, 0);

                List<RelativeDirection> receivedProtein = receive[0].chars()
                        .mapToObj(i -> (char) i)
                        .map(RelativeDirection::fromChar)
                        .collect(Collectors.toList());

                bestPopulation.addGensToGenpool(receivedProtein);
            }

            bestPopulation.evaluate(PRIMARY_SEQUENCE);

            bestPopulation.saveResults(0);

            for (Structure structure : bestPopulation.getStructures()) {
                System.out.println("Best protein of one solution has neighbors: " + structure.getValidNeighborCount());
            }

            System.out.println("Received a new best protein: " + bestPopulation.getBestProtein().getValidNeighborCount());

            return bestPopulation;

        } else {
            String[] send = new String[1];
            send[0] = population.getBestProtein().getRelativeDirections()
                    .stream()
                    .map(RelativeDirection::getCharacter)
                    .map(Object::toString)
                    .collect(Collectors.joining());

            MPI.COMM_WORLD.Send(send, 0, 1, MPI.OBJECT, 0, 0);

            return null;
        }
    }

    private static void addAndReplaceNeighborGenes(Population population, List<List<RelativeDirection>> neighborGenepool) {
        List<List<RelativeDirection>> genepool = population.getGenepool();
        Collections.shuffle(genepool);
        for (List<RelativeDirection> genom : neighborGenepool) {
            genepool.remove(0);
            genepool.add(genom);
        }
    }

    private static void sendPoolToNeighbor(int to, int individualAmountToSend, Population population) {
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

        MPI.COMM_WORLD.Send(send, 0, 1, MPI.OBJECT, to, 0);
    }

    private static List<List<RelativeDirection>> receivePoolFromNeighbor(int from) {
        String[] receive = new String[1];
        MPI.COMM_WORLD.Recv(receive, 0, 1, MPI.OBJECT, from, 0);

        int singleSequenceSize = PRIMARY_SEQUENCE.length();
        Iterable<String> splittedGenepool = Splitter.fixedLength(singleSequenceSize).split(receive[0]);

        return StreamSupport.stream(splittedGenepool.spliterator(), true)
                .map(singleGenom ->
                        singleGenom.chars()
                                .mapToObj(i -> (char) i)
                                .map(RelativeDirection::fromChar)
                                .collect(Collectors.toList()))
                .collect(Collectors.toList());
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
