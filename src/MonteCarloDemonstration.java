import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 000359041
 * Modified by Mychaylo Tatarynov to be multithreaded.
 * @web http://java-buddy.blogspot.com/2015/07/apply-animaton-in-javafx-charts-with.html
 */

public class MonteCarloDemonstration extends Application {
    public static final int logicalCoreCount = 1; //Runtime.getRuntime().availableProcessors();
    public static final int iterations = 1_000_000;
    public static int iterationsRan = 0;
    public static boolean processing = true;

    public static SecureRandom random = new SecureRandom();
    public static double[] group = new double[ 1576 ]; //  0-1576 for possible yahtzee scores
    public static int over150=0, over200=0;
    public static int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
    public static int scores = 0;
    
    @Override
    public void start(Stage primaryStage) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String, Number> barChart
                = new BarChart<>(xAxis, yAxis);
        barChart.setCategoryGap(0);
        barChart.setBarGap(0);
        barChart.setAnimated(true);
        barChart.setMinHeight(Screen.getPrimary().getVisualBounds().getMaxY()*.65);

        xAxis.setLabel("Score");
        yAxis.setLabel("# of Games");
 
        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Yahtzee Score Distribution");
        
        // initialize the bars, all scores to 0%
        for ( int i = 0; i < group.length; i++ ) {
            series1.getData().add(new XYChart.Data<String, Double>(Integer.toString(i), group[i]));
        }
 
        // Intial bar chart is just the axis with no data
        barChart.getData().addAll(series1);

        /*
        Label labelInfo = new Label();
        labelInfo.setText(
                "java.version: " + System.getProperty("java.version") + "\n"
                        + "javafx.runtime.version: " + System.getProperty("javafx.runtime.version")
        );
        */

        Label labelCnt = new Label();
        labelCnt.setText("Iterations: " + iterationsRan);

        Label labelAnimated = new Label();
        labelAnimated.setText("Min Score:\t\t"+min+"\nMax Score:\t\t"+max+"\nGames>=150:\t\t"+over150+"\nGames>=200:\t\t"+over200+"\nAverage Score:\t\t0");
 
        VBox vBox = new VBox();
        vBox.setLayoutX(Screen.getPrimary().getVisualBounds().getMaxX()*0.50-50);
        vBox.setLayoutY(Screen.getPrimary().getVisualBounds().getMaxY()*.8+25);
        vBox.getChildren().addAll(barChart, labelCnt, labelAnimated);
        vBox.setSpacing(20);
        vBox.setAlignment(Pos.CENTER);
 
        StackPane root = new StackPane();
        root.getChildren().add(vBox);
 
        Scene scene = new Scene(root, Screen.getPrimary().getBounds().getMaxX()*.8, Screen.getPrimary().getBounds().getMaxY()*.8);

        primaryStage.setTitle("Yahtzee Score Histogram");
        primaryStage.setScene(scene);
        primaryStage.show();


        //Apply Animating Data in Charts
        //ref: http://docs.oracle.com/javafx/2/charts/bar-chart.htm
        //"Animating Data in Charts" section
        Timeline timeline = new Timeline();

        timeline.getKeyFrames().add(
                new KeyFrame( Duration.millis(1), (ActionEvent actionEvent) -> {
                    //Avoiding divide by 0 by just not updating at the very start.
                    if (iterationsRan == 0) {
                        return;
                    }

                    for (int i = 0; i < group.length; i++) {
                        ((XYChart.Data) series1.getData().get(i)).setYValue(group[i]);
                    }

                    labelCnt.setText("iterations: " + iterationsRan + (iterationsRan == iterations ? " (finished)" : ""));

                    String text =   String.format("Min Score:\t\t%d Max Score:\t\t%d Games>=150:\t\t%d(%2.3f%%) Games>=200:\t\t%d (%2.3f%%) Average Score:\t\t%d", min, max, over150, (double)over150 / iterationsRan * 100, over200,(double)over200 / iterationsRan * 100, scores / iterationsRan);

                    labelAnimated.setText(text);

                    //We stop the timeline here rather than in startProcessing() because we want to make sure that the
                    //last iterations are updated on the timeline.
                    if (!processing) {
                        timeline.stop();
                    }
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        barChart.setAnimated(false);

        startProcessing();
    }

    /**
     * Starts the multithreaded processing for the simulation. It launches enough work threads to max out your CPU and an
     * extra controller thread that waits for the work threads to finish before switching off the processing variable.
     */
    public void startProcessing() {
        new Thread(() -> {
            List<Thread> threads = new ArrayList<>();

            //Creating a thread for every logical core. There is no point in making more threads and would only raise overhead.
            for (int i = 0; i < logicalCoreCount; i ++ ) {
                int workload = getWorkloadForThread(i, logicalCoreCount);

                Thread thread = new Thread(() -> {
                    for (int j = 0; j < workload; j++) {
                        addScore(new YahtzeeStrategy().play());
                    }
                });

                threads.add(thread);
                thread.start();
            }

            //Make sure that every thread is finished before continuing.
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            processing = false;
        }).start();
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
    public int getWorkloadForThread(int threadNum, int totalThreads) {
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

        group[score]++;
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}
