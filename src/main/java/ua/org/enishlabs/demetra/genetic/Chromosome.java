package ua.org.enishlabs.demetra.genetic;

import org.apache.hadoop.io.WritableComparable;
import org.encog.engine.network.activation.ActivationFunction;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author EniSh
 *         Date: 12.03.12
 */
public class Chromosome {
	private int layerCount;
	private int neuronsDensity;
	private ActivationFunction activationFunction;

	public Chromosome() {
	}

	public Chromosome(int layerCount, int neuronsDensity, ActivationFunction activationFunction) {
		this.layerCount = layerCount;
		this.neuronsDensity = neuronsDensity;
		this.activationFunction = activationFunction;
	}

	public int getLayerCount() {
		return layerCount;
	}

	public int getNeuronsDensity() {
		return neuronsDensity;
	}

	public ActivationFunction getActivationFunction() {
		return activationFunction.clone();
	}

    @Override
    public String toString() {
        return "Chromosome{" +
                "layerCount=" + layerCount +
                ", neuronsDensity=" + neuronsDensity +
                ", activationFunction=" + activationFunction +
                '}';
    }
}
