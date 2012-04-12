package ua.org.enishlabs.demetra.genetic;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:enisher@gmail.com">Artem Orobes</a>
 * @since 10.04.12
 */
public class SurviveStrategy {

  public List<Chromosome> filter(List<ChromosomeRate> rates) {
    final List<Chromosome> nextPopulation = new ArrayList<Chromosome>(rates.size());
    nextPopulation.addAll(Collections2.transform(rates.subList(0, (int) Math.sqrt(rates.size())), new Function<ChromosomeRate, Chromosome>() {
      @Override
      public Chromosome apply(ChromosomeRate input) {
        return input.getChromosome();
      }
    }));
    return nextPopulation;
  }
}