package ua.org.enishlabs.demetra.genetic;

import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;

/**
 * @author EniSh
 *         Date: 13.04.12
 */
public class OrganizmBuilder {
    public BasicNetwork build(Chromosome chromosome) {
        System.out.println("Creation organism from " + chromosome);
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
        return network;
    }
}
