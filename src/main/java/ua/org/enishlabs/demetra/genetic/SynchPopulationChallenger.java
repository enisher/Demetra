package ua.org.enishlabs.demetra.genetic;

import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;

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
        return new RateFunction().evaluate(new Trainer().train(network));
    }
}
