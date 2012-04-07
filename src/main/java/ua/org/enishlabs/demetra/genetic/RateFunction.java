package ua.org.enishlabs.demetra.genetic;

/**
 * User: dr_Enish
 * Date: 07.04.12
 */
public class RateFunction {

    public double evaluate(double error) {
        return Double.isNaN(error) ? Double.NEGATIVE_INFINITY : -error;
    }
}
