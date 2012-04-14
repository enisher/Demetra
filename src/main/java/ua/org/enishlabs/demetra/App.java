package ua.org.enishlabs.demetra;

import org.encog.engine.network.activation.ActivationBiPolar;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import ua.org.enishlabs.demetra.genetic.*;
import ua.org.enishlabs.demetra.genetic.distributed.DistributedPopulationChallenger;

import java.util.*;

public class App {
    public static double[][] input = {{0., 0.}, {0., 1.}, {1., 0.}, {1., 1.}};
    public static double[][] ideal = {{0.}, {1.}, {1.}, {0.}};
    private static final int POPULATION_SIZE = 10;
    private static final Random r = new Random();

    private static final PopulationChallenger populationChallenger = new DistributedPopulationChallenger();

    public static void main(String[] args) throws Exception {
        GlobalConfig.programArgs = args;

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
            final BasicNetwork cachedOrganism = bestRate.getCachedOrganism();
            System.out.println("Result");
            System.out.println("Best chromosome: " + bestRate.getChromosome());
            System.out.println("0 0 = " + cachedOrganism.compute(new BasicMLData(new double[]{.0, .0})).getData(0));
            System.out.println("0 1 = " + cachedOrganism.compute(new BasicMLData(new double[]{.0, 1.})).getData(0));
            System.out.println("1 0 = " + cachedOrganism.compute(new BasicMLData(new double[]{1., .0})).getData(0));
            System.out.println("1 1 = " + cachedOrganism.compute(new BasicMLData(new double[]{1., 1.})).getData(0));
        }
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
    return new Chromosome(a.getLayerCount() + r.nextInt(10) - 5, a.getNeuronsDensity(), a.getActivationFunction());
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

    v1 = r.nextDouble();
    final ActivationFunction newActivationFunction;
    if (v1 < .5) {
      newActivationFunction = a.getActivationFunction();
    } else {
      newActivationFunction = a.getActivationFunction();
    }

    return new Chromosome(newLayerCount, newNeuronDensity, newActivationFunction);
  }

    private static List<Chromosome> generateFirstPopulation() {
        final List<Chromosome> population = new ArrayList<Chromosome>();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new Chromosome(r.nextInt(30)+1, r.nextInt(50)+1, choseActivationFunction(r)));
        }
        return population;
    }

    private static ActivationFunction choseActivationFunction(Random r) {
        final List<? extends ActivationFunction> functions = Arrays.asList(new ActivationTANH(), new ActivationSigmoid(), new ActivationBiPolar());

        final int v = r.nextInt(functions.size());
        return functions.get(v);
    }

}
