package org.hda.gaf.algorithm.geneticalgorithm;

import lombok.NoArgsConstructor;
import org.hda.gaf.algorithm.Population;
import org.hda.gaf.algorithm.PopulationGenerator;
import org.hda.gaf.algorithm.output.GraphicOutput;
import org.hda.gaf.algorithm.selectionalgorithm.SelectionAlgorithm;

/**
 * Created by marcus on 08.05.16.
 */
@NoArgsConstructor
public abstract class GeneticAlgorithm {

    protected long startTime;
    protected boolean documentsStatistic = false;

    protected String primarySequence;

    protected int populationAmount;

    protected float mutationRate;
    protected float crossoverRate;

    protected boolean calculateHammingDistance;

    protected int currentGeneration;
    protected int totalGeneration;

    protected boolean printWhileGenerating;

    private GraphicOutput frame;
    private SelectionAlgorithm selectionAlgorithm;

    public GeneticAlgorithm startedAt(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public GeneticAlgorithm printsWhileGenerating(boolean printWhileGenerating) {
        this.printWhileGenerating = printWhileGenerating;
        return this;
    }

    public GeneticAlgorithm documentsStatistic(boolean documentsStatistic) {
        this.documentsStatistic = documentsStatistic;
        return this;
    }

    public GeneticAlgorithm usesPrimarySequence(String primarySequence) {
        this.primarySequence = primarySequence;
        return this;
    }

    public GeneticAlgorithm hasPopulationAmountOf(int populationAmount) {
        this.populationAmount = populationAmount;
        return this;
    }

    public GeneticAlgorithm hasMutationRateOf(float mutationRate) {
        this.mutationRate = mutationRate;
        return this;
    }

    public GeneticAlgorithm hasCrossoverRateOf(float crossoverRate) {
        this.crossoverRate = crossoverRate;
        return this;
    }

    public GeneticAlgorithm calculatesHammingDistance(boolean hammingDistance) {
        this.calculateHammingDistance = hammingDistance;
        return this;
    }

    public GeneticAlgorithm usesSelectionAlgorithm(SelectionAlgorithm selectionAlgorithm) {
        this.selectionAlgorithm = selectionAlgorithm;
        return this;
    }

    public Population generateStartPopulation() {
        this.totalGeneration = 0;

        PopulationGenerator populationGenerator = new PopulationGenerator(primarySequence.length(), populationAmount);
        return populationGenerator.generateStartPopulation();
    }

    public Population runAlgorithm(Population population) {
        population.usesSelectionAlgorithm(selectionAlgorithm);
        population.setStartTime(startTime);
        population.setDocumentsStatistic(documentsStatistic);

        population.evaluate(primarySequence);

        this.currentGeneration = 0;

        population.calculateHammingDistance(calculateHammingDistance);
        population.saveResults(totalGeneration);

        if (printWhileGenerating) {
            initializeFrame();
            paint(population);
        }

        return generateTillLimit(population);
    }

    private void initializeFrame() {
        frame = new GraphicOutput();
    }

    private void paint(final Population population) {
        frame.setPopulation(population);
        frame.repaint();
    }

    protected void performAlgorithm(Population population) {
        population.select(); //selection
        population.crossover(crossoverRate);
        population.mutate(mutationRate);

        population.evaluate(primarySequence);

        population.calculateHammingDistance(calculateHammingDistance);
        population.saveResults(totalGeneration);

        if (printWhileGenerating) {
            paint(population);
        }
    }

    protected abstract Population generateTillLimit(Population population);

    public int getCurrentGeneration() {
        return currentGeneration;
    }
}
