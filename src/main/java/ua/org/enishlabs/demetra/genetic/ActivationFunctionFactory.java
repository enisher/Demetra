package ua.org.enishlabs.demetra.genetic;

import org.encog.engine.network.activation.ActivationBiPolar;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;

import java.util.*;

/**
 * @author EniSh
 *         Date: 20.04.12
 */
public class ActivationFunctionFactory {

    private static final List<? extends ActivationFunction> functions = Arrays.asList(new ActivationTANH(), new ActivationSigmoid(), new ActivationBiPolar());
    private static final Map<String, ActivationFunction> functionMap = new HashMap<String, ActivationFunction>(functions.size());

    static {
        for (ActivationFunction function : functions) {
            functionMap.put(function.getClass().getSimpleName(), function);
        }
    }

    public static ActivationFunction choseActivationFunction(Random r) {
        final int v = r.nextInt(functions.size());
        return functions.get(v);
    }

    public static ActivationFunction resolveFunctionByName(String name) {
        return functionMap.get(name).clone();
    }
}
