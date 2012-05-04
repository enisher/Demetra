package ua.org.enishlabs.demetra;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.ml.data.basic.BasicMLDataSet;
import ua.org.enishlabs.demetra.genetic.*;
import ua.org.enishlabs.demetra.genetic.distributed.DistributedPopulationChallenger;
import ua.org.enishlabs.demetra.genetic.distributed.TrainingSetProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class App {
    private static final int POPULATION_SIZE = 10;
    private static final Random r = new Random();
    private static final TrainingSetProvider TRAINING_SET_PROVIDER = new TrainingSetProvider();
    private static final PopulationChallenger populationChallenger = new DistributedPopulationChallenger();

    public static void main(String[] args) throws Exception {
        GlobalConfig.programArgs = args;

        prepareTrainingSet();

        System.out.println("Preparing first generation...");
        List<Chromosome> population = generateFirstPopulation();
        System.out.println("First generation has been prepared.");

        System.out.println("Start challenging generation #0");
        List<ChromosomeRate> rates = challenge(population);
        System.out.println("Generation #0 challenged");
        double currentRate = Collections.max(rates).getRate();
        System.out.println("Rate: " + currentRate);

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
//            final BasicNetwork cachedOrganism = bestRate.getCachedOrganism();
//            System.out.println("0 0 = " + cachedOrganism.compute(new BasicMLData(new double[]{.0, .0})).getData(0));
//            System.out.println("0 1 = " + cachedOrganism.compute(new BasicMLData(new double[]{.0, 1.})).getData(0));
//            System.out.println("1 0 = " + cachedOrganism.compute(new BasicMLData(new double[]{1., .0})).getData(0));
//            System.out.println("1 1 = " + cachedOrganism.compute(new BasicMLData(new double[]{1., 1.})).getData(0));
        }
    }

    private static void prepareTrainingSet() {
        double[][] input = {{0., 0.}, {0., 1.}, {1., 0.}, {1., 1.}};
        double[][] ideal = {{0.}, {1.}, {1.}, {0.}};
        final BasicMLDataSet dataSet = new BasicMLDataSet(input, ideal);
        TRAINING_SET_PROVIDER.save(dataSet);
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
        return new Chromosome(a.getLayerCount() + r.nextInt(10) - 5, a.getNeuronsDensity(), a.getActivationFunctions());
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

    private static List<Chromosome> generateFirstPopulation() {
        final List<Chromosome> population = new ArrayList<Chromosome>();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            final int layerCount = r.nextInt(30) + 1;
            final ArrayList<ActivationFunction> activationFunctions = new ArrayList<ActivationFunction>();
            for (int j = 0; j < layerCount; j++) {
                activationFunctions.add(ActivationFunctionFactory.choseActivationFunction(r));
            }

            population.add(new Chromosome(layerCount, r.nextInt(50) + 1, activationFunctions));
        }
        return population;
    }


}
