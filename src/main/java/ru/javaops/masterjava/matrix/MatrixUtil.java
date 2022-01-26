package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        int thatColumn[] = new int[matrixSize];

        class ColumnsMatrixC {
            private Integer columnNumber;
            private int[] column;

            public ColumnsMatrixC(int columnNumber, int[] column) {
                this.columnNumber = columnNumber;
                this.column = column;
            }

            public int getColumnNumber() {
                return columnNumber;
            }

            public int[] getColumn() {
                return column;
            }
        }
        CompletionService<ColumnsMatrixC> completionService = new ExecutorCompletionService<>(executor);
        ArrayList<Future<ColumnsMatrixC>> futures = new ArrayList<>();
        for (int j = 0; j < matrixSize; j++) {
            final int finalJ = j;
            futures.add(completionService.submit(() -> {
                for (int k = 0; k < matrixSize; k++) {
                    thatColumn[k] = matrixB[k][finalJ];
                }
                final int[] row = new int[matrixSize];
                for (int i = 0; i < matrixSize; i++) {
                    final int thisRow[] = matrixA[i];
                    int summand = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        summand += thisRow[k] * thatColumn[k];
                    }
                    row[i] = summand;
                }
                return new ColumnsMatrixC(finalJ, row);
            }));
        }
        while (!futures.isEmpty()) {
            Future<ColumnsMatrixC> future = completionService.take();
            for (int i = 0; i < matrixSize; i++) {
                matrixC[i][future.get().getColumnNumber()] = future.get().getColumn()[i];
            }
            futures.remove(future);
        }
        return matrixC;
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        int thatColumn[] = new int[matrixSize];

//        for (int i = 0; i < matrixSize; i++) {
//            for (int j = 0; j < matrixSize; j++) {
//                int sum = 0;
//                for (int k = 0; k < matrixSize; k++) {
//                    sum += matrixA[i][k] * matrixB[k][j];
//                }
//                matrixC[i][j] = sum;
//            }
//        }
        try {
            for (int j = 0; ; j++) {
                for (int k = 0; k < matrixSize; k++) {
                    thatColumn[k] = matrixB[k][j];
                }

                for (int i = 0; i < matrixSize; i++) {
                    int thisRow[] = matrixA[i];
                    int summand = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        summand += thisRow[k] * thatColumn[k];
                    }
                    matrixC[i][j] = summand;
                }
            }
        } catch (IndexOutOfBoundsException ex) {
        }

        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
