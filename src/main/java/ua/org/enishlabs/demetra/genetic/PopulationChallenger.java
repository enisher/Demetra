package ua.org.enishlabs.demetra.genetic;

import java.util.List;

/**
 * @author EniSh
 *         Date: 12.04.12
 */
public interface PopulationChallenger {
    List<ChromosomeRate> challenge(List<Chromosome> population);
}
