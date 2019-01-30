package org.hda.gaf.algorithm.geneticalgorithm;

import org.hda.gaf.algorithm.Population;

import java.time.Instant;

/**
 * Created by marcus on 31.05.16.
 */
public class TimeLimitedAlgorithm extends GeneticAlgorithm{

    private Long time;
    private Long endTime;

    public GeneticAlgorithm usesTimeLimit(long time) {
        this.time = time;
        return this;
    }

    private void setupStart() {
        Long startTime = Instant.now().toEpochMilli();
        endTime = startTime + time;
    }

    @Override
    protected Population generateTillLimit(Population population){
        setupStart();
        while (!isOver()) {
            currentGeneration++;
            totalGeneration++;

            performAlgorithm(population);
        }

        return population;
    }

    private boolean isOver() {
        long currentTime = Instant.now().toEpochMilli();
        return currentTime >= endTime;
    }
}
