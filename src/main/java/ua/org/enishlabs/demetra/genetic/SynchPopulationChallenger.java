package ua.org.enishlabs.demetra.genetic;

import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import ua.org.enishlabs.demetra.App;
import ua.org.enishlabs.demetra.genetic.distributed.TrainingSetProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author EniSh
 *         Date: 12.04.12
 */
public class SynchPopulationChallenger implements PopulationChallenger {
    private static TrainingSetProvider trainingSetProvider = new TrainingSetProvider();

    @Override
    public List<ChromosomeRate> challenge(List<Chromosome> population) {
        final List<ChromosomeRate> rates = new ArrayList<ChromosomeRate>(population.size());
        for (Chromosome chromosome : population) {
            final BasicNetwork network = new OrganizmBuilder(2,1).build(chromosome);

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
        final BasicMLDataSet trainSet = trainingSetProvider.load();
        return new RateFunction().evaluate(new Trainer(trainSet).train(network));
    }
}
