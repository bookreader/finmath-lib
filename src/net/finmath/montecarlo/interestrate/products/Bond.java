/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 10.02.2004
 */
package net.finmath.montecarlo.interestrate.products;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.RandomVariable;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationInterface;
import net.finmath.stochastic.RandomVariableInterface;

/**
 * This class implements the valuation of a zero coupon bond.
 * 
 * @author Christian Fries
 * @version 1.1
 */
public class Bond extends AbstractLIBORMonteCarloProduct {
	private double maturity;

    /**
	 * @param maturity The maturity given as double.
	 */
	public Bond(double maturity) {
		super();
		this.maturity = maturity;
	}

    /**
     * This method returns the value random variable of the product within the specified model, evaluated at a given evalutationTime.
     * Note: For a lattice this is often the value conditional to evalutationTime, for a Monte-Carlo simulation this is the (sum of) value discounted to evaluation time.
     * Cashflows prior evaluationTime are not considered.
     * 
     * @param evaluationTime The time on which this products value should be observed.
     * @param model The model used to price the product.
     * @return The random variable representing the value of the product discounted to evaluation time
     * @throws net.finmath.exception.CalculationException
     */
    @Override
    public RandomVariableInterface getValue(double evaluationTime, LIBORModelMonteCarloSimulationInterface model) throws CalculationException {
		
		// Get random variables
        RandomVariableInterface	numeraire				= model.getNumeraire(maturity);
        RandomVariableInterface	monteCarloProbabilities	= model.getMonteCarloWeights(maturity);

        // Calculate numeraire relative value
        RandomVariableInterface values = new RandomVariable(maturity, 1.0);
        values = values.div(numeraire).mult(monteCarloProbabilities);
        
        // Convert back to values
        RandomVariableInterface	numeraireAtZero					= model.getNumeraire(evaluationTime);
        RandomVariableInterface	monteCarloProbabilitiesAtZero	= model.getMonteCarloWeights(evaluationTime);
        values = values.mult(numeraireAtZero).div(monteCarloProbabilitiesAtZero);

		// Return values
		return values;	
	}

	/**
	 * @return Returns the maturity.
	 */
	public double getMaturity() {
		return maturity;
	}

	/**
	 * @param maturity The maturity to set.
	 */
	public void setMaturity(double maturity) {
		this.maturity = maturity;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return super.toString() + "\n" + "maturity: " + maturity;
	}
}
