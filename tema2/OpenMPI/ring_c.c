#include <stdio.h>
#include "mpi.h"

int main(int argc, char *argv[])
{
    int rank, size, next, prev, message, tag = 201;

    MPI_Init(&argc, &argv);


    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    printf("%d - %d\n", rank, size);

    next = (rank + 1) % size;
    prev = (rank + size - 1) % size;

    if (0 == rank) {
        message = 10;

        printf("Process 0 sending message %d to process %d\n",
               message, next);
        MPI_Send(&message, 1, MPI_INT, next, tag, MPI_COMM_WORLD);
    }

    while (1) {
        MPI_Recv(&message, 1, MPI_INT, prev, tag, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
		printf("Process %d receiving message %d from process %d\n", rank, message, prev);
	
        if (0 == rank) {
            --message;
            printf("Process 0 decremented value: %d\n", message);
        }

		printf("Process %d sending message %d to process %d\n", rank, message, next);
        MPI_Send(&message, 1, MPI_INT, next, tag, MPI_COMM_WORLD);
        
		if (0 == message) {
            printf("Process %d exiting\n", rank);
            break;
        }
    }

    if (0 == rank) {
        MPI_Recv(&message, 1, MPI_INT, prev, tag, MPI_COMM_WORLD,
                 MPI_STATUS_IGNORE);
    }

    MPI_Finalize();
    return 0;
}
