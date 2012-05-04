package ua.org.enishlabs.demetra.genetic;

import org.apache.hadoop.io.Text;
import org.encog.neural.networks.BasicNetwork;

/**
 * @author EniSh
 *         Date: 03.04.12
 */
public final class ChromosomeRate implements Comparable<ChromosomeRate> {
	private final Chromosome chromosome;
	private final double rate;
    private BasicNetwork cachedOrganism;

	public ChromosomeRate(Chromosome chromosome, double rate) {
		this.chromosome = chromosome;
		this.rate = rate;
	}

    public ChromosomeRate(Chromosome chromosome, double rate, BasicNetwork cachedOrganism) {
        this.chromosome = chromosome;
        this.rate = rate;
        this.cachedOrganism = cachedOrganism;
    }

    public double getRate() {
		return rate;
	}

	public Chromosome getChromosome() {
		return chromosome;
	}

    public BasicNetwork getCachedOrganism() {
        return cachedOrganism;
    }

    public void setCachedOrganism(BasicNetwork cachedOrganism) {
        this.cachedOrganism = cachedOrganism;
    }

    @Override
	public int compareTo(ChromosomeRate o) {
		return Double.compare(rate, o.rate);
	}

    public String toStream() {
        return "Rate " + rate + " " + chromosome.toStream();
    }
}
