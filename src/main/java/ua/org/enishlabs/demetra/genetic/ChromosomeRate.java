package ua.org.enishlabs.demetra.genetic;

/**
 * @author EniSh
 *         Date: 03.04.12
 */
public final class ChromosomeRate implements Comparable<ChromosomeRate> {
	private final Chromosome chromosome;
	private final double rate;

	public ChromosomeRate(Chromosome chromosome, double rate) {
		this.chromosome = chromosome;
		this.rate = rate;
	}

	public double getRate() {
		return rate;
	}

	public Chromosome getChromosome() {
		return chromosome;
	}

	@Override
	public int compareTo(ChromosomeRate o) {
		return Double.compare(rate, o.rate);
	}
}
