package org.hda.gaf;

import com.google.common.base.Stopwatch;
import mpi.MPI;
import org.hda.gaf.algorithm.GeneticAlgorithmGenerator;
import org.hda.gaf.algorithm.Population;
import org.hda.gaf.algorithm.geneticalgorithm.GeneticAlgorithm;
import org.hda.gaf.mpj.MPJAlgorithmExecutor;
import org.hda.gaf.mpj.MPJPopulationCollector;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.hda.gaf.DefaultOptions.POPULATION_AMOUNT;

public class Main {

    public static void main(String[] args) {
        int rank, size;

        MPI.Init(args);
        size = MPI.COMM_WORLD.Size();
        rank = MPI.COMM_WORLD.Rank();

        Stopwatch s = Stopwatch.createStarted();

        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithmGenerator().fromArgs(args, rank);

        Population population = MPJAlgorithmExecutor.executeAlgorithm(geneticAlgorithm, rank, size);

        int globalGenerationCount = MPJAlgorithmExecutor.getGlobalGenerationCount();
        Population bestPopulation = MPJPopulationCollector.collectPopulationWithBestProteins(rank, size, population);

        if (rank == 0) {
            System.out.println("Current generation without size: " + geneticAlgorithm.getCurrentGeneration());
            System.out.println("Size: " + size);
//            int calculatedGenerations = geneticAlgorithm.getCurrentGeneration() * size;
            int calculatedIndividuals = POPULATION_AMOUNT * globalGenerationCount;
            System.out.println("Calculation took: " + s.elapsed(TimeUnit.MILLISECONDS) + " ms");
            System.out.println("Calculation generations: " + globalGenerationCount);
            System.out.println("Calculation individuals: " + calculatedIndividuals);


            bestPopulation.printStatusOfCurrentGeneration(globalGenerationCount, bestPopulation.getBestProtein());
            saveChart(population);
        }

        MPI.Finalize();
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
