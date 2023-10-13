/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.*;

/**
 *
 * @author Stephen Adams
 * Modified by Mychaylo Tatarynov to be multithreaded.
 */
public class playGame {
    public static final int logicalCoreCount = Runtime.getRuntime().availableProcessors();
    // Use the below for debugging your code
    //public static int iterations = 1;
    // Use the below for running the scoring code
    public static int iterations = 1_000_000;

    public static int iterationsRan = 0;
    public static int scores = 0;
    public static int min = Integer.MAX_VALUE;
    public static int max = Integer.MIN_VALUE;
    public static int over150 = 0;
    public static int over200 = 0;

    public static void main( String[] args ) throws InterruptedException {
        double startTime = System.currentTimeMillis();

        startProcessing();

        double elapsedTime = System.currentTimeMillis() - startTime;

        System.out.printf(  "User: %s\t\tIterations: %d\t\t\tMin Score: %d\t\tMax Score: %d\t\tAverage Score: %.2f\t\t" +
                            "Games>150: %.2f%%\t\tGames>200: %.2f%%\t\tTime: %.2fs\n",YahtzeeStrategy.username,
                            iterationsRan, min, max, (double)scores/ iterationsRan, (double)over150/ iterationsRan * 100,
                            (double)over200/ iterationsRan * 100, elapsedTime / 1000 );
    }

    public static void startProcessing() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();

        //Creating a thread for every logical core. There is no point in making more threads and would only raise overhead.
        for (int i = 0; i < logicalCoreCount; i ++ ) {
            int workload = getWorkloadForThread(i, logicalCoreCount);

            Thread thread = new Thread(() -> {
                for (int j = 0; j < workload; j++) {
                    addScore( new YahtzeeStrategy().play() );
                }
            });

            threads.add(thread);
            thread.start();
        }

        //Make sure that every thread is finished before continuing.
        for (Thread thread : threads) {
            thread.join();
        }
    }

    /**
     * Returns the number of times the thread should run its work while making sure that every iteration is accounted
     * for by adding any remainders to the last thread. E.g. if you need 1,000,000 iteration and have a 12 thread CPU,
     * the iterations can't be divided evenly and each will get 83,333 iterations, leaving 4 leftover. This method would
     * make sure to give the last thread 83,337 to compensate.
     * @param threadNum The number of the work thread.
     * @param totalThreads The total number of work threads.
     * @return The number of times the thread should run its work.
     */
    public static int getWorkloadForThread(int threadNum, int totalThreads) {
        int workload = iterations / totalThreads;
        int remainder = iterations % totalThreads;
        return threadNum == totalThreads - 1 ? workload + remainder : workload;
    }

    /***
     * Adding scores is delegated to a synchronized method to avoid race conditions.
     * @param score The score to add.
     */
    public static synchronized void addScore(int score) {
        iterationsRan++;
        scores += score;
        max = Math.max(score, max);
        min = Math.min(score, min);
        if (score>=200) over200++;
        else if (score>=150) over150++;

        if (iterationsRan % 100000 == 0) {
            System.out.printf("%d iterations finished.\n", iterationsRan);
        }
    }
}
