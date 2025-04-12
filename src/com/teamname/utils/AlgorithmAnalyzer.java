package com.teamname.utils;

import java.util.function.Consumer;

/**
 * Utility for analyzing algorithm performance
 */
public class AlgorithmAnalyzer {
    
    /**
     * Measures the execution time of an algorithm
     * @param algorithm The algorithm to measure
     * @param input The input data for the algorithm
     * @param <T> The type of input data
     * @return The execution time in milliseconds
     */
    public static <T> long measureExecutionTime(Consumer<T> algorithm, T input) {
        long startTime = System.nanoTime();
        algorithm.accept(input);
        long endTime = System.nanoTime();
        
        return (endTime - startTime) / 1_000_000; // Convert to milliseconds
    }
    
    /**
     * Compares the execution time of two algorithms
     * @param algorithm1 The first algorithm
     * @param algorithm2 The second algorithm
     * @param input The input data for both algorithms
     * @param <T> The type of input data
     * @return A comparison result with execution times
     */
    public static <T> AlgorithmComparisonResult compareAlgorithms(
            Consumer<T> algorithm1, 
            Consumer<T> algorithm2, 
            T input, 
            String algorithm1Name, 
            String algorithm2Name) {
        
        long time1 = measureExecutionTime(algorithm1, input);
        long time2 = measureExecutionTime(algorithm2, input);
        
        return new AlgorithmComparisonResult(
                algorithm1Name, 
                algorithm2Name, 
                time1, 
                time2);
    }
    
    /**
     * Result class for algorithm comparison
     */
    public static class AlgorithmComparisonResult {
        private final String algorithm1Name;
        private final String algorithm2Name;
        private final long algorithm1Time;
        private final long algorithm2Time;
        
        public AlgorithmComparisonResult(
                String algorithm1Name, 
                String algorithm2Name, 
                long algorithm1Time, 
                long algorithm2Time) {
            this.algorithm1Name = algorithm1Name;
            this.algorithm2Name = algorithm2Name;
            this.algorithm1Time = algorithm1Time;
            this.algorithm2Time = algorithm2Time;
        }
        
        public String getFasterAlgorithm() {
            return algorithm1Time <= algorithm2Time ? algorithm1Name : algorithm2Name;
        }
        
        public long getTimeDifference() {
            return Math.abs(algorithm1Time - algorithm2Time);
        }
        
        public double getSpeedupRatio() {
            return algorithm1Time >= algorithm2Time 
                ? (double) algorithm1Time / algorithm2Time 
                : (double) algorithm2Time / algorithm1Time;
        }
        
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append("Algorithm Comparison Results:\n");
            result.append("- ").append(algorithm1Name).append(": ").append(algorithm1Time).append(" ms\n");
            result.append("- ").append(algorithm2Name).append(": ").append(algorithm2Time).append(" ms\n");
            result.append("- Faster algorithm: ").append(getFasterAlgorithm()).append("\n");
            result.append("- Time difference: ").append(getTimeDifference()).append(" ms\n");
            result.append("- Speedup ratio: ").append(String.format("%.2f", getSpeedupRatio())).append("x");
            
            return result.toString();
        }
    }
}