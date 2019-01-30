package org.hda.gaf.algorithm.geneticalgorithm;

import org.hda.gaf.algorithm.Population;

/**
 * Created by marcus on 31.05.16.
 */
public class GenerationLimitedAlgorithm extends GeneticAlgorithm {

    private Integer generationAmount;

    public GeneticAlgorithm usesGenerationLimit(int generationAmount) {
        this.generationAmount = generationAmount;
        return this;
    }

    @Override
    protected Population generateTillLimit(Population population) {
        while (currentGeneration < generationAmount) {
            currentGeneration++;
            totalGeneration++;

            performAlgorithm(population);

//            System.out.println(currentGeneration + " generation. Current best protein. Valid neighbors: " + population.getBestProtein().getValidNeighborCount()
//                    + " with overlaps: " + population.getBestProtein().getOverlappCounter());
        }

        return population;
    }

    @Override
    protected void setupStart() {

    }
}
