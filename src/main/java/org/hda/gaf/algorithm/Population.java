package org.hda.gaf.algorithm;

import org.hda.gaf.algorithm.crossover.CrossoverAlgorithm;
import org.hda.gaf.algorithm.evaluation.FitnessCalculator;
import org.hda.gaf.algorithm.evaluation.counter.HammingDistanceCounter;
import org.hda.gaf.algorithm.evaluation.direction.RelativeDirection;
import org.hda.gaf.algorithm.evaluation.node.Structure;
import org.hda.gaf.algorithm.mutation.MutationAlgorithm;
import org.hda.gaf.algorithm.selectionalgorithm.SelectionAlgorithm;
import lombok.Getter;
import lombok.Setter;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Created by marcus on 07.05.16.
 */
@Getter
@Setter
public class Population {

    private long startTime;
    private boolean documentsStatistic;

    private List<List<RelativeDirection>> genepool;

    private List<Structure> structures;
    private Structure bestProtein;

    private double totalRelativeFitness;
    private double averageRelativeFitness;

    private PopulationStatistic statistic;

    private DefaultCategoryDataset lineChartDataset;
    private SelectionAlgorithm selectionAlgorithm;

    public Population(int populationSize) {
        genepool = new ArrayList<>(populationSize);
        structures = new ArrayList<>(populationSize);
        bestProtein = new Structure();
        statistic = new PopulationStatistic();

        lineChartDataset = new DefaultCategoryDataset();
    }

    public void addGensToGenpool(List<RelativeDirection> gens) {
        genepool.add(gens);
    }

    public void addStructure(Structure structure) {
        structures.add(structure);
    }

    public void saveResults(int currentGeneration) {
        Structure currentBestProtein = calculateStatisticAndGetCurrentBestProtein();

        checkForNewBestProtein(currentBestProtein);

        statistic.setGeneration(currentGeneration);
        statistic.setBestProteinInGeneration(currentBestProtein);

        saveAndPrintResults(currentGeneration, currentBestProtein);
    }

    private void checkForNewBestProtein(Structure currentBestProtein) {
        if (currentBestProtein.getAbsoluteFitness() > 0) {
            if (currentBestProtein.getAbsoluteFitness() > bestProtein.getAbsoluteFitness()) {
                bestProtein = currentBestProtein;
            }
        } else {
//            System.out.println("No valid structure was generated.");
        }
    }

    private Structure calculateStatisticAndGetCurrentBestProtein() {
        double totalAbsoluteFitness = structures.stream().mapToDouble(Structure::getAbsoluteFitness).sum();
        int totalNeighborCounter = structures.stream().mapToInt(Structure::getNeighborCounter).sum();
        int totalOverlapCounter = structures.stream().mapToInt(Structure::getOverlappCounter).sum();
        double totalHammingDistance = structures.stream().mapToDouble(Structure::getAverageHammingDistance).sum();
        Optional<Structure> currentBestProtein = structures.stream().max(Comparator.comparing(Structure::getAbsoluteFitness));

        double averageAbsoluteFitness = totalAbsoluteFitness / (double) structures.size();
        double averageNeighborCounter = (double) totalNeighborCounter / (double) structures.size();
        double averageOverlapCounter = (double) totalOverlapCounter / (double) structures.size();
        double averageHammingDistance = totalHammingDistance / (double) structures.size();

        statistic.setTotalFitness(totalAbsoluteFitness);
        statistic.setAverageFitness(averageAbsoluteFitness);
        statistic.setAverageNeighborCounter(averageNeighborCounter);
        statistic.setAverageOverlapCounter(averageOverlapCounter);
        statistic.setTotalHammingDistance(totalHammingDistance);
        statistic.setAverageHammingDistance(averageHammingDistance);
        statistic.setBestProtein(bestProtein);

        return currentBestProtein.orElse(null);
    }

    private void saveAndPrintResults(int currentGeneration, Structure currentBestProtein) {
        if (documentsStatistic) {
            printStatusOfCurrentGeneration(currentGeneration, currentBestProtein);
        }
//        bestProtein.printStructure();
        saveValuesForChart(currentGeneration, currentBestProtein);
    }

    public void printStatusOfCurrentGeneration(int currentGeneration, Structure currentBestProtein) {
        System.out.println("Generation " + currentGeneration + ":");
        System.out.println("  Total Fitness: " + statistic.getTotalFitness());
        System.out.println("  Average Fitness " + statistic.getAverageFitness());
        System.out.println("  Average neighbor count: " + statistic.getAverageNeighborCounter());
        System.out.println("  Average overlap count: " + statistic.getAverageOverlapCounter());
        System.out.println("  Total Hamming distance: " + statistic.getTotalHammingDistance());
        System.out.println("  Average Hamming distance: " + statistic.getAverageHammingDistance());
        System.out.println("  Best overall: Fitness: " + statistic.getBestProtein().getAbsoluteFitness());
        System.out.println("  Best overall: Overlaps " + statistic.getBestProtein().getOverlappCounter());
//        System.out.println("  Best overall: Neighbor count: " + bestProtein.getNeighborCounter());
        System.out.println("  Best overall: Valid neighbor count: " + statistic.getBestProtein().getValidNeighborCount());
        System.out.println("  Best in generation: absoluteFitness: " + statistic.getBestProteinInGeneration().getAbsoluteFitness());
        System.out.println("  Best in generation: Overlaps " + statistic.getBestProteinInGeneration().getOverlappCounter());
//        System.out.println("  Best in generation: Neighbor count: " + currentBestProtein.getNeighborCounter());
        System.out.println("  Best in generation: Valid neighbor count: " + statistic.getBestProteinInGeneration().getValidNeighborCount());
    }

    private void saveValuesForChart(int currentGeneration, Structure currentBestProtein) {
        //        lineChartDataset.addValue(totalAbsoluteFitness, "total absoluteFitness", String.valueOf(currentGeneration));
        lineChartDataset.addValue(statistic.getAverageFitness(), "average absoluteFitness", String.valueOf(currentGeneration));
        lineChartDataset.addValue(statistic.getBestProtein().getAbsoluteFitness(), "best overall absoluteFitness", String.valueOf(currentGeneration));
        lineChartDataset.addValue(statistic.getBestProteinInGeneration().getAbsoluteFitness(), "best absoluteFitness in generation", String.valueOf(currentGeneration));
        lineChartDataset.addValue(statistic.getAverageHammingDistance(), "average hamming", String.valueOf(currentGeneration));
    }

    public void select() {
        selectionAlgorithm.selectOnPopulation(this);
    }

    public void crossover(float crossoverRate) {
        CrossoverAlgorithm crossoverAlgorithm = new CrossoverAlgorithm(crossoverRate, genepool.size());
        this.genepool = crossoverAlgorithm.crossoverGenepoolOfPopulation(genepool);
    }

    public void mutate(float mutationRate) {
        MutationAlgorithm mutationAlgorithm = new MutationAlgorithm();
        this.genepool = mutationAlgorithm.mutate(genepool, mutationRate);
    }

    public void evaluate(String primarySequence) {
        new FitnessCalculator(primarySequence).calculateFitnessOfPopulation(this);
    }

    public List<Structure> calculateHammingDistance(boolean calcHammingDistance) {
        HammingDistanceCounter hammingDistanceCounter = new HammingDistanceCounter();
        structures = hammingDistanceCounter.calculateHammingDistance(calcHammingDistance, structures);
        return structures;
    }

    public void usesSelectionAlgorithm(SelectionAlgorithm selectionAlgorithm) {
        this.selectionAlgorithm = selectionAlgorithm;
    }
}
