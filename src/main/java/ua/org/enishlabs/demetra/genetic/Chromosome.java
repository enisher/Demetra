package ua.org.enishlabs.demetra.genetic;

import org.apache.hadoop.io.WritableComparable;
import org.encog.engine.network.activation.ActivationFunction;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

/**
 * @author EniSh
 *         Date: 12.03.12
 */
public class Chromosome {
	private int layerCount;
	private int neuronsDensity;
    private List<ActivationFunction> activationFunctions;

	public Chromosome() {
	}

	public Chromosome(int layerCount, int neuronsDensity, List<ActivationFunction> activationFunctions) {
		this.layerCount = layerCount;
		this.neuronsDensity = neuronsDensity;
		this.activationFunctions = activationFunctions;
	}

	public int getLayerCount() {
		return layerCount;
	}

	public int getNeuronsDensity() {
		return neuronsDensity;
	}

    public List<ActivationFunction> getActivationFunctions() {
        return activationFunctions;
    }

    @Override
    public String toString() {
        return "Chromosome{" +
                "layerCount=" + layerCount +
                ", neuronsDensity=" + neuronsDensity +
                ", activationFunctions=" + activationFunctions +
                '}';
    }

    public String toStream() {
        final StringBuilder sb = new StringBuilder( "Chromosome " + layerCount + " " + neuronsDensity + " ");
        sb.append(activationFunctions.size()).append(" ");
        for (ActivationFunction activationFunction : activationFunctions) {
            sb.append(activationFunction.getClass().getSimpleName()).append(" ");
        }

        return sb.toString();
    }
}
