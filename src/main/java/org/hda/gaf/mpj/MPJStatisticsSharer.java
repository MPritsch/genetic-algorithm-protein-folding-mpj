package org.hda.gaf.mpj;

import mpi.MPI;
import org.hda.gaf.algorithm.Population;

public class MPJStatisticsSharer {

    private static int globalGenerationCount = 0;

    public static int getGlobalGenerationCount() {
        return globalGenerationCount;
    }

    public static void shareStatistics(Population population, int rank, int size) {
        int localGenerationCount = population.getStatistic().getGeneration();

        if (rank == 0) {
            globalGenerationCount += localGenerationCount;

            for (int from = 1; from < size; from++) {
                int[] receive = new int[1];

                MPI.COMM_WORLD.Recv(receive, 0, 1, MPI.INT, from, 0);

                globalGenerationCount += receive[0];
            }
        } else {

            int[] send = new int[]{localGenerationCount};
            MPI.COMM_WORLD.Send(send, 0, 1, MPI.INT, 0, 0);
        }
    }
}
