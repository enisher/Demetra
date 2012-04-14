package ua.org.enishlabs.demetra.genetic.distributed;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
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
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.neural.networks.BasicNetwork;
import ua.org.enishlabs.demetra.GlobalConfig;
import ua.org.enishlabs.demetra.genetic.*;

import javax.xml.bind.annotation.XmlElementDecl;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * @author EniSh
 *         Date: 12.04.12
 */
public class DistributedPopulationChallenger extends Configured implements Tool, PopulationChallenger {

    private Path in = new Path("temp/in.txt");
    private Path out = new Path("temp/out.txt");

    @Override
    public List<ChromosomeRate> challenge(List<Chromosome> population) {
        writePopulationToHDFS(population);

        try {
            ToolRunner.run(new Configuration(), this, GlobalConfig.programArgs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return readRateFromHDFS();
    }

    private void writePopulationToHDFS(List<Chromosome> population) {
        try {
            FileSystem fs = FileSystem.get(new Configuration());
            BufferedWriter br=new BufferedWriter(new OutputStreamWriter(fs.create(in,true)));

            for (Chromosome chromosome : population) {
                br.write(chromosome.toStream() + "\n");
            }

            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ChromosomeRate> readRateFromHDFS() {
        return null;
    }

    @Override
    public int run(String[] args) throws Exception {
        final Configuration conf = getConf();
        Job job = new Job(conf, "ua.org.enishlabs.demetra");
        job.setJarByClass(DistributedPopulationChallenger.class);

        FileInputFormat.setInputPaths(job, in);
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

        private static final Trainer trainer = new Trainer();
        private final OrganizmBuilder organizmBuilder = new OrganizmBuilder();

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            final String[] params = value.toString().split(" ");

            final Chromosome chromosome = new Chromosome(Integer.valueOf(params[1]), Integer.valueOf(params[2]), new ActivationTANH());
            final BasicNetwork network = organizmBuilder.build(chromosome);

            final double error = trainer.train(network);

            context.write(new Text(new ChromosomeRate(chromosome, error, network).toStream()), new Text(""));

        }
    }

    public static class Reduce extends Reducer<Text, Text, Text, Text> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for (Text value : values) {
                context.write(key, value);
            }
        }
    }
}
