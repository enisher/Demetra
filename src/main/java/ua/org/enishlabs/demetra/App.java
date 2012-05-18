package ua.org.enishlabs.demetra;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.ml.data.basic.BasicMLDataSet;
import ua.org.enishlabs.demetra.genetic.*;
import ua.org.enishlabs.demetra.genetic.distributed.DistributedPopulationChallenger;
import ua.org.enishlabs.demetra.genetic.distributed.TrainingSetProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.*;

public class App {
    private static final int POPULATION_SIZE = 10;
    private static final Random r = new Random();
    private static final TrainingSetProvider TRAINING_SET_PROVIDER = new TrainingSetProvider();
    private static final PopulationChallenger populationChallenger = new DistributedPopulationChallenger();

    public static void main(String[] args) throws Exception {
        GlobalConfig.programArgs = args;

        final BasicMLDataSet trainingSet = prepareTrainingSet(args[0]);

        System.out.println("Preparing first generation...");
        List<Chromosome> population = generateFirstPopulation(trainingSet);
        System.out.println("First generation has been prepared.");

        final long startTimestamp = System.currentTimeMillis();

        System.out.println("Start challenging generation #0");
        List<ChromosomeRate> rates = challenge(population);
        System.out.println("Generation #0 challenged");
        double currentRate = Collections.max(rates).getRate();
        System.out.println("Rate: " + currentRate);

        final FileWriter writer = new FileWriter(new File(args[1]));
        writer.write((System.currentTimeMillis() - startTimestamp) + " " + currentRate + "\n");

        int iteration = 1;
        double prevRate = Double.NEGATIVE_INFINITY;
        while (iteration < 100 || (currentRate - prevRate) > 0.00001) {
            System.out.println("Preparing population #" + iteration);
            prepareNextPopulation(population, rates);
            System.out.println("Population #" + iteration + " has been prepared.");

            System.out.println("Start challenging generation #" + iteration);
            rates = challenge(population);
            System.out.println("Generation #" + iteration + " challenged");
            prevRate = currentRate;

            currentRate = Collections.max(rates).getRate();

            System.out.println("Rate: " + currentRate);

            iteration++;
            final ChromosomeRate bestRate = Collections.max(rates);
            System.out.println("Result");
            System.out.println("Best chromosome: " + bestRate.getChromosome());
            writer.write((System.currentTimeMillis() - startTimestamp) + " " + currentRate + "\n");

//            final BasicNetwork cachedOrganism = bestRate.getCachedOrganism();
//            System.out.println("0 0 = " + cachedOrganism.compute(new BasicMLData(new double[]{.0, .0})).getData(0));
//            System.out.println("0 1 = " + cachedOrganism.compute(new BasicMLData(new double[]{.0, 1.})).getData(0));
//            System.out.println("1 0 = " + cachedOrganism.compute(new BasicMLData(new double[]{1., .0})).getData(0));
//            System.out.println("1 1 = " + cachedOrganism.compute(new BasicMLData(new double[]{1., 1.})).getData(0));
        }
        writer.close();
    }

    private static BasicMLDataSet prepareTrainingSet(String pathToDataFile) {
//        double[][] input = {{0., 0.}, {0., 1.}, {1., 0.}, {1., 1.}};
//        double[][] ideal = {{0.}, {1.}, {1.}, {0.}};

        Scanner in = null;
        try {
            in = new Scanner(new File(pathToDataFile));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        in.useLocale(new Locale("RU"));

        final int N = in.nextInt();

        final int inputSize = in.nextInt();
        double[][] input = new double[N][inputSize];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < inputSize; j++) {
                input[i][j] = in.nextDouble();
            }
        }

        final int outputSize = in.nextInt();
        double[][] ideal = new double[N][outputSize];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < outputSize; j++) {
                ideal[i][j] = in.nextDouble();
            }
        }

        final BasicMLDataSet dataSet = new BasicMLDataSet(input, ideal);
        TRAINING_SET_PROVIDER.save(dataSet);
        return dataSet;
    }

    private static List<ChromosomeRate> challenge(List<Chromosome> population) {
        return populationChallenger.challenge(population);
    }

    private static void prepareNextPopulation(List<Chromosome> population, List<ChromosomeRate> rates) {
        //Update population
        Collections.sort(rates);
        final List<Chromosome> nextPopulation = new SurviveStrategy().filter(rates);
        while (nextPopulation.size() < POPULATION_SIZE) {
            if (r.nextDouble() < .8) {
                final Chromosome a = population.get(r.nextInt(population.size()));
                final Chromosome b = population.get(r.nextInt(population.size()));

                nextPopulation.add(crossover(a, b));
            } else {
                final Chromosome a = population.get(r.nextInt(population.size()));

                nextPopulation.add(mutation(a));
            }
        }
    }

    private static Chromosome mutation(Chromosome a) {
        int dLC = r.nextInt(10) - 5;
        if (a.getLayerCount() + dLC <= 2) {
            dLC = a.getLayerCount() - 2; // 2 is a minimal possible layer count
        }

        final List<ActivationFunction> activationFunctions = new ArrayList<ActivationFunction>(a.getActivationFunctions());
        for (int i = 0; i < activationFunctions.size(); i++) {
            if (r.nextDouble() < .05) {
                activationFunctions.set(i, ActivationFunctionFactory.choseActivationFunction(r));
            }
        }
        if (dLC > 0) {
            activationFunctions.add(ActivationFunctionFactory.choseActivationFunction(r));
        } else {
            for (int i = 0, j = activationFunctions.size() - 1; i < -dLC; i++, j--) {
                activationFunctions.remove(j);
            }
        }
        return new Chromosome(a.getLayerCount() + dLC, a.getNeuronsDensity() + r.nextInt(10) - 5, activationFunctions);
    }

    private static Chromosome crossover(Chromosome a, Chromosome b) {
        final int newLayerCount;
        double v1 = r.nextDouble();
        if (v1 < .3) {
            newLayerCount = a.getLayerCount();
        } else if (v1 < .6) {
            newLayerCount = b.getLayerCount();
        } else {
            newLayerCount = (a.getLayerCount() + b.getLayerCount()) / 2;
        }

        v1 = r.nextDouble();
        final int newNeuronDensity;
        if (v1 < .3) {
            newNeuronDensity = a.getNeuronsDensity();
        } else if (v1 < .6) {
            newNeuronDensity = b.getNeuronsDensity();
        } else {
            newNeuronDensity = (a.getNeuronsDensity() + b.getNeuronsDensity()) / 2;
        }

        final List<ActivationFunction> activationFunctions = new ArrayList<ActivationFunction>();
        for (int i = 0; i < newLayerCount; i++) {
            if (i < a.getLayerCount() && i < b.getLayerCount()) {
                v1 = r.nextDouble();
                if (v1 < .5) {
                    activationFunctions.add(a.getActivationFunctions().get(i));
                } else {
                    activationFunctions.add(b.getActivationFunctions().get(i));
                }
            } else if (i < a.getLayerCount()) {
                activationFunctions.add(a.getActivationFunctions().get(i));
            } else {
                activationFunctions.add(b.getActivationFunctions().get(i));
            }
        }

        return new Chromosome(newLayerCount, newNeuronDensity, activationFunctions);
    }

    private static List<Chromosome> generateFirstPopulation(BasicMLDataSet trainingSet) {
        final List<Chromosome> population = new ArrayList<Chromosome>();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            final int layerCount = r.nextInt(20) + 2;
            final ArrayList<ActivationFunction> activationFunctions = new ArrayList<ActivationFunction>();
            for (int j = 0; j < layerCount; j++) {
                activationFunctions.add(ActivationFunctionFactory.choseActivationFunction(r));
            }

            int ll = Math.round(trainingSet.getRecordCount() / 10 - trainingSet.getIdealSize() - trainingSet.getInputSize());
            int lh = Math.round(trainingSet.getRecordCount() / 2 - trainingSet.getIdealSize() - trainingSet.getInputSize());
            if (ll < 0) {
                ll = 20;
                lh = 50;
            }

            population.add(new Chromosome(layerCount, r.nextInt(lh - ll) + ll, activationFunctions));
        }
        return population;
    }


}
