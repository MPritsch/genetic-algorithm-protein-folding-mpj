package org.hda.gaf.mpj;

import mpi.MPI;
import org.hda.gaf.algorithm.Population;
import org.hda.gaf.algorithm.evaluation.direction.RelativeDirection;

import java.util.List;
import java.util.stream.Collectors;

import static org.hda.gaf.DefaultOptions.PRIMARY_SEQUENCE;

public class MPJPopulationCollector {

    public static Population collectPopulationWithBestProteins(int rank, int size, Population population) {

        System.out.println("Rank " + rank + ", best protein of has valid neighbors: " + population.getBestProtein().getValidNeighborCount()
                + " with overlaps: " + population.getBestProtein().getOverlappCounter());


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
}
