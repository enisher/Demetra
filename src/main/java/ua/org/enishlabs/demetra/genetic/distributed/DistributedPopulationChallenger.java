package ua.org.enishlabs.demetra.genetic.distributed;

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
import ua.org.enishlabs.demetra.GlobalConfig;
import ua.org.enishlabs.demetra.genetic.Chromosome;
import ua.org.enishlabs.demetra.genetic.ChromosomeRate;
import ua.org.enishlabs.demetra.genetic.PopulationChallenger;

import javax.xml.bind.annotation.XmlElementDecl;
import java.io.IOException;
import java.util.List;

/**
 * @author EniSh
 *         Date: 12.04.12
 */
public class DistributedPopulationChallenger extends Configured implements Tool, PopulationChallenger {
    @Override
    public List<ChromosomeRate> challenge(List<Chromosome> population) {
        writePopulationToHDFS(population);

        final int res;
        try {
            res = ToolRunner.run(new Configuration(), this, GlobalConfig.programArgs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return readRateFromHDFS();
    }

    private void writePopulationToHDFS(List<Chromosome> population) {

    }

    private List<ChromosomeRate> readRateFromHDFS() {
        return null;
    }

    @Override
    public int run(String[] args) throws Exception {
        final Configuration conf = getConf();
        Job job = new Job(conf, "ua.org.enishlabs.demetra");
        job.setJarByClass(DistributedPopulationChallenger.class);

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
