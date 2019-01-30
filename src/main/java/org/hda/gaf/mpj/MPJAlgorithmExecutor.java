package org.hda.gaf.mpj;

import org.hda.gaf.algorithm.Population;
import org.hda.gaf.algorithm.evaluation.direction.RelativeDirection;
import org.hda.gaf.algorithm.geneticalgorithm.GeneticAlgorithm;

import java.util.Collections;
import java.util.List;

import static org.hda.gaf.DefaultOptions.POPULATION_AMOUNT;

public class MPJAlgorithmExecutor {

    public static int getGlobalGenerationCount() {
        return MPJStatisticsSharer.getGlobalGenerationCount();
    }

    public static Population executeAlgorithm(GeneticAlgorithm geneticAlgorithm, int rank, int size) {

        Population population = geneticAlgorithm.generateStartPopulation();

        int nextNeighbor = (rank + 1) % size;
        int prevNeighbor = (rank + size - 1) % size;
        int individualAmountToSend = POPULATION_AMOUNT / 5;

        for (int i = 0; i < 5; i++) {
            population = geneticAlgorithm.runAlgorithm(population);

            List<List<RelativeDirection>> neighborGenepool = MPJPopulationSharer.shareAndReceiveNeighborPool(rank, nextNeighbor, prevNeighbor, individualAmountToSend, population);

            addAndReplaceNeighborGenes(population, neighborGenepool);

            if (rank == 0) {
                System.out.println("Done with " + i + ". iteration.");
            }
        }

        MPJStatisticsSharer.shareStatistics(population, rank, size);

        return population;
    }

    private static void addAndReplaceNeighborGenes(Population population, List<List<RelativeDirection>> neighborGenepool) {
        List<List<RelativeDirection>> genepool = population.getGenepool();
        for (List<RelativeDirection> genom : neighborGenepool) {
            genepool.remove(0);
            genepool.add(genom);
        }
    }
}
