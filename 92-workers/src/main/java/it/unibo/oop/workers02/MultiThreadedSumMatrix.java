package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class MultiThreadedSumMatrix implements SumMatrix{

    private final int nthread;

    /**
     * 
     * @param nthread
     *            no. of thread performing the sum.
     */
    public MultiThreadedSumMatrix(final int nthread) {
        this.nthread = nthread;
    }

    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startRow, startColumn;
        private final int nelem;
        private double res;

        /**
         * Build a new worker.
         * 
         * @param matrix
         *            the list to sum
         * @param startpos
         *            the initial position for this worker
         * @param nelem
         *            the no. of elems to sum up for this worker
         */
        Worker(final double[][] matrix, final int startRow, final int startColumn, final int nelem) {            
            super();            
            this.matrix = matrix;
            this.startRow = startRow;
            this.startColumn = startColumn;
            this.nelem = nelem;
        }

        @Override
        public void run() {
            int counter = 0, i = 0, j = 0;            
            for(i = startRow; i < matrix.length && counter < this.nelem; i++) {
                for(j = startColumn; j < matrix[0].length && counter < this.nelem; j++) {
                    this.res += matrix[i][j];
                }
            }
            System.out.println("Working from position (" + startRow + "," + startColumn + ") to position (" + i + "," + j + ")");
        }

        /**
         * Returns the result of summing up the integers within the list.
         * 
         * @return the sum of every element in the array
         */
        public Double getResult() {
            return this.res;
        }

    }

    @Override
    public double sum(double[][] matrix) {
        if(matrix == null) {
            return 0;
        }
        
        final int nElements = matrix.length * matrix[0].length;
        final int size = nElements % nthread + nElements / nthread;
        int row = 0, col = 0;

        final List<Worker> workers = new ArrayList<>(nthread);
        for (int start = 0; start < matrix.length; start += size) {
            workers.add(new Worker(matrix, row, col, size));            
            for(int counter = 0; row < matrix.length && counter < size; row++) {
                for(; col < matrix[0].length && counter < size; col++) {
                }
            }
        }
        for (final Worker w: workers) {
            w.start();
        }
        
        long sum = 0;
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return sum;        
    }
}
