package org.hda.gaf.mpj;

import com.google.common.base.Splitter;
import mpi.MPI;
import org.hda.gaf.algorithm.Population;
import org.hda.gaf.algorithm.evaluation.direction.RelativeDirection;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hda.gaf.DefaultOptions.PRIMARY_SEQUENCE;

public class MPJPopulationSharer {


    public static List<List<RelativeDirection>> shareAndReceiveNeighborPool(int rank, int nextNeighbor, int prevNeighbor, int individualAmountToSend, Population population) {
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
}
