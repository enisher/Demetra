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
import org.apache.hadoop.util.ToolRunner;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationGaussian;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import ua.org.enishlabs.demetra.genetic.Chromosome;
import ua.org.enishlabs.demetra.genetic.ChromosomeRate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Hello world!
 */
public class App extends Configured implements Tool {
	private static double[][] input = {{0., 0.}, {0.,1.},{1., 0.}, {1.,1.}};
	private static double[][] ideal = {{0.}, {1.},{1.}, {0.}};
	private static final int POPULATION_SIZE = 10;
    private static final Random r = new Random();

	public static void main(String[] args) throws Exception {

		final List<Chromosome> population = new ArrayList<Chromosome>();

		Random r = new Random();
		for (int i = 0; i < POPULATION_SIZE; i++) {
			population.add(new Chromosome(r.nextInt(30), r.nextInt(50), choseActivationFunction(r)));
		}

		final List<ChromosomeRate> rates = new ArrayList<ChromosomeRate>(population.size());
		for (Chromosome chromosome : population) {
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
			rates.add(new ChromosomeRate(chromosome, error));
		}

		//Update population
		Collections.sort(rates);
        final List<Chromosome> nextPopulation = filterToNextGeneration(rates);
		
		while (nextPopulation.size() < POPULATION_SIZE) {
			if (r.nextDouble()< .8) {
                final Chromosome a = population.get(r.nextInt(population.size()));
                final Chromosome b = population.get(r.nextInt(population.size()));

                nextPopulation.add(crossover(a, b));
            } else {
                final Chromosome a = population.get(r.nextInt(population.size()));

                nextPopulation.add(mutation(a));
            }
		}

//		final int res = ToolRunner.run(new Configuration(), new App(), args);
//		System.exit(res);
	}

    private static Chromosome mutation(Chromosome a) {
        return new Chromosome( a.getLayerCount() + r.nextInt(10) - 5, a.getNeuronsDensity(), a.getActivationFunction());
    }

    private static Chromosome crossover(Chromosome a, Chromosome b) {
        final int newLayerCount;
        final double v1 = r.nextDouble();
        if (v1 < .3) {
            newLayerCount = a.getLayerCount();
        }

        int newNeuronDensity;
        ActivationFunction newActivationFunction;
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
		final Train train = new Backpropagation(network, new BasicMLDataSet(input, ideal));

		int iteration = 1;
		
		do {
			train.iteration();
			iteration++;
		} while ((iteration < 500000) && (train.getError() > 0.05));
		
		System.out.println("Iteration #" + iteration + " Error:" + train.getError());
		return train.getError();
	}
	

	private static ActivationFunction choseActivationFunction(Random r) {
		final List<? extends ActivationFunction> functions = Arrays.asList(new ActivationTANH(), new ActivationGaussian());

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

		System.exit(job.waitForCompletion(true)?0:1);

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
