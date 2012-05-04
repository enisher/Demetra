package ua.org.enishlabs.demetra.genetic;

import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import ua.org.enishlabs.demetra.App;

import java.util.ArrayList;
import java.util.List;

/**
 * @author EniSh
 *         Date: 12.04.12
 */
public class SynchPopulationChallenger implements PopulationChallenger {
    @Override
    public List<ChromosomeRate> challenge(List<Chromosome> population) {
        final List<ChromosomeRate> rates = new ArrayList<ChromosomeRate>(population.size());
        for (Chromosome chromosome : population) {
            final BasicNetwork network = new OrganizmBuilder().build(chromosome);

            final double error = train(network);
            rates.add(new ChromosomeRate(chromosome, error, network));
        }
        return rates;
    }

    /**
     * @param network to train
     * @return error
     */
    private static double train(BasicNetwork network) {
        final BasicMLDataSet trainSet = readTrainSetFromHDFS();
        return new RateFunction().evaluate(new Trainer(trainSet).train(network));
    }

    private static BasicMLDataSet readTrainSetFromHDFS() {
        final double[][] input = App.input;
        final double[][] ideal = App.ideal;
        return new BasicMLDataSet(input, ideal);
    }
}
