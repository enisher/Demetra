package ua.org.enishlabs.demetra.genetic;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;

import java.util.List;

/**
 * @author EniSh
 *         Date: 13.04.12
 */
public class OrganizmBuilder {
    private final int inputsCount;
    private final int outputsCount;

    public OrganizmBuilder(int inputsCout, int outputsCount) {
        this.inputsCount = inputsCout;
        this.outputsCount = outputsCount;
    }

    public BasicNetwork build(Chromosome chromosome) {
        System.out.println("Creation organism from " + chromosome);
        final BasicNetwork network = new BasicNetwork();

        final List<ActivationFunction> activationFunctions = chromosome.getActivationFunctions();

        //Add input layer
        network.addLayer(new BasicLayer(activationFunctions.get(0), true, inputsCount));

        for (int i = 0; i < chromosome.getLayerCount(); i++) {
            network.addLayer(new BasicLayer(activationFunctions.get(i+1), true, chromosome.getNeuronsDensity()));
        }

        //Add output layer
        network.addLayer(new BasicLayer(activationFunctions.get(activationFunctions.size()-1), false, outputsCount));

        network.getStructure().finalizeStructure();
        network.reset();
        return network;
    }
}
