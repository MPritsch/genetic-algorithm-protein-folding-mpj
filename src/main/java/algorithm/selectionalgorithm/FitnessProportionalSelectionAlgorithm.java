package algorithm.selectionalgorithm;

import algorithm.Population;
import algorithm.evaluation.node.Structure;
import org.apache.commons.math3.util.Pair;

import java.util.List;

/**
 * Created by marcus on 09.06.16.
 */
public class FitnessProportionalSelectionAlgorithm extends SelectionAlgorithm {

    public void selectOnPopulation(Population population) {
        List<Pair> weightedStructures = buildWeightedStructures(population.getStructures(), population.getTotalAbsoluteFitness());

        List<Structure> selection = pickWeightedStructuresRandomly(weightedStructures, weightedStructures.size());

        buildGenepoolOfSelection(population, selection);
    }
}
