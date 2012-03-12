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
public class DNA implements WritableComparable<DNA> {
	private int layerCount;
	private Class<? extends ActivationFunction> activationFunction;

	@Override
	public int compareTo(DNA o) {
		 return 0;
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {

	}

	@Override
	public void readFields(DataInput dataInput) throws IOException {

	}
}
