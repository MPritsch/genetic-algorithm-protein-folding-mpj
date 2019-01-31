package org.hda.gaf;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
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
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hda.gaf.DefaultOptions.*;

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
            long totalTime = s.elapsed(TimeUnit.MILLISECONDS);

            System.out.println("Current generation without size: " + geneticAlgorithm.getCurrentGeneration());
            System.out.println("Size: " + size);
//            int calculatedGenerations = geneticAlgorithm.getCurrentGeneration() * size;
            int calculatedIndividuals = POPULATION_AMOUNT * globalGenerationCount;
            System.out.println("Calculation took: " + s.elapsed(TimeUnit.MILLISECONDS) + " ms");
            System.out.println("Calculation generations: " + globalGenerationCount);
            System.out.println("Calculation individuals: " + calculatedIndividuals);

            saveCSV(totalTime, globalGenerationCount, calculatedIndividuals, size, args);

            bestPopulation.printStatusOfCurrentGeneration(globalGenerationCount, bestPopulation.getBestProtein());
            saveChart(population);
        }

        MPI.Finalize();
    }


    private static void saveCSV(long totalTime, int globalGenerationCount, int calculatedIndividuals, int size, String[] args) {
        try {
            FileWriter fileWriter = new FileWriter("results.csv", true);

            String arguments = String.join(" ", args);
            List<String> commands = Lists.newArrayList(args);

            Integer timeLimit = getCountFromCommand(commands, "--time", TIME_LIMIT);
            Integer generationCount = getCountFromCommand(commands, "--generation", GENERATION_AMOUNT);


            String csvLine = String.format("%d;%s;%d;%d;%s;%s;%s;\n", totalTime, size, globalGenerationCount, calculatedIndividuals, timeLimit, generationCount, arguments);

            fileWriter.append(csvLine);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Could not save results to file. Check them above.");
        }
    }

    private static int getCountFromCommand(List<String> commands, String command, long defaultValue) {
        int commandIndex = commands.indexOf(command);

        if (commandIndex != -1) {
            if (commands.size() > commandIndex) {
                return Integer.valueOf(commands.get(commandIndex + 1));
            }
        }
        return Math.toIntExact(defaultValue);
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
