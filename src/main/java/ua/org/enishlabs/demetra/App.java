package ua.org.enishlabs.demetra;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.encog.engine.network.activation.ActivationBiPolar;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import ua.org.enishlabs.demetra.genetic.Chromosome;
import ua.org.enishlabs.demetra.genetic.ChromosomeRate;
import ua.org.enishlabs.demetra.genetic.RateFunction;
import ua.org.enishlabs.demetra.genetic.Trainer;

import java.io.IOException;
import java.util.*;

public class App extends Configured implements Tool {
    public static double[][] input = {{0., 0.}, {0., 1.}, {1., 0.}, {1., 1.}};
    public static double[][] ideal = {{0.}, {1.}, {1.}, {0.}};
    private static final int POPULATION_SIZE = 10;
    private static final Random r = new Random();

    public static void main(String[] args) throws Exception {

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
//		final int res = ToolRunner.run(new Configuration(), new App(), args);
//		System.exit(res);
    }

    private static void prepareNextPopulation(List<Chromosome> population, List<ChromosomeRate> rates) {
        //Update population
        Collections.sort(rates);
        final List<Chromosome> nextPopulation = filterToNextGeneration(rates);
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

    private static List<ChromosomeRate> challenge(List<Chromosome> population) {
        final List<ChromosomeRate> rates = new ArrayList<ChromosomeRate>(population.size());
        for (Chromosome chromosome : population) {
            System.out.println("Creation organism from " + chromosome);
            final BasicNetwork network = new BasicNetwork();

            //Add input layer
            network.addLayer(new BasicLayer(chromosome.getActivationFunction(), true, 2));

            for (int i = 0; i < chromosome.getLayerCount(); i++) {
                network.addLayer(new BasicLayer(chromosome.getActivationFunction(), true, chromosome.getNeuronsDensity()));
            }

            //Add output layer
            network.addLayer(new BasicLayer(chromosome.getActivationFunction(), false, 1));

            network.getStructure().finalizeStructure();
            network.reset();

            final double error = train(network);
            rates.add(new ChromosomeRate(chromosome, error, network));
        }
        return rates;
    }

    private static List<Chromosome> generateFirstPopulation() {
        final List<Chromosome> population = new ArrayList<Chromosome>();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new Chromosome(r.nextInt(30)+1, r.nextInt(50)+1, choseActivationFunction(r)));
        }
        return population;
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

    private static List<Chromosome> filterToNextGeneration(List<ChromosomeRate> rates) {
        final List<Chromosome> nextPopulation = new ArrayList<Chromosome>(rates.size());
        nextPopulation.addAll(Collections2.transform(rates.subList(0, (int) Math.sqrt(rates.size())), new Function<ChromosomeRate, Chromosome>() {
            @Override
            public Chromosome apply(ChromosomeRate input) {
                return input.getChromosome();
            }
        }));
        return nextPopulation;
    }

    /**
     * @param network to train
     * @return error
     */
    private static double train(BasicNetwork network) {
        return new RateFunction().evaluate(new Trainer().train(network));
    }



    private static ActivationFunction choseActivationFunction(Random r) {
        final List<? extends ActivationFunction> functions = Arrays.asList(new ActivationTANH(), new ActivationSigmoid(), new ActivationBiPolar());

        final int v = r.nextInt(functions.size());
        return functions.get(v);
    }

    @Override
    public int run(String[] args) throws Exception {
        final Configuration conf = getConf();
        Job job = new Job(conf, "ua.org.enishlabs.demetra");
        job.setJarByClass(App.class);

        Path in = new Path(args[0]);
        FileInputFormat.setInputPaths(job, in);
        Path out = new Path(args[1]);
        FileOutputFormat.setOutputPath(job, out);

        job.setMapperClass(MapClass.class);
        job.setReducerClass(Reduce.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);

        return 0;
    }

    public static class MapClass extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

        }
    }

    public static class Reduce extends Reducer<Text, Text, Text, Text> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

        }
    }
}
