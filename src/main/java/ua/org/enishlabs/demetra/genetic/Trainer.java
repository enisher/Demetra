package ua.org.enishlabs.demetra.genetic;

import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import ua.org.enishlabs.demetra.App;

/**
 * User: dr_Enish
 * Date: 07.04.12
 */
public class Trainer {
    public double train(BasicNetwork network) {
        final Train train = new Backpropagation(network, new BasicMLDataSet(App.input, App.ideal));

        int iteration = 0;

        do {
            train.iteration();
            if (iteration % 2000 == 0) {
                System.out.println("Iteration #" + iteration + " Error:" + train.getError());
            }
            iteration++;
        } while ((iteration < 7000) && (train.getError() > 0.05));

        System.out.println("Iteration #" + iteration + " Error:" + train.getError());
        return train.getError();
    }

}
