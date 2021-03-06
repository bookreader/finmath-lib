/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 20.05.2006
 */
package net.finmath.montecarlo.interestrate.modelplugins;

import net.finmath.functions.LinearAlgebra;
import net.finmath.time.TimeDiscretizationInterface;


/**
 * @author Christian Fries
 *
 */
public class LIBORCorrelationModelExponentialDecay extends LIBORCorrelationModel {
	
	private final int			numberOfFactors;
	private double		a;
	private final boolean		isCalibrateable;
	

	private double[][]	correlationMatrix;
	private double[][]	factorMatrix;
	
	
	public LIBORCorrelationModelExponentialDecay(TimeDiscretizationInterface timeDiscretization, TimeDiscretizationInterface liborPeriodDiscretization, int numberOfFactors, double a, boolean isCalibrateable) {
		super(timeDiscretization, liborPeriodDiscretization);

		this.numberOfFactors	= numberOfFactors;
		this.a					= a;
		this.isCalibrateable	= isCalibrateable;

		initialize(numberOfFactors, a);
	}

	@Override
	public void setParameter(double[] parameter) {
		if(!isCalibrateable) return;

		a = parameter[0];

		initialize(numberOfFactors, a);
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public LIBORCorrelationModelExponentialDecay(TimeDiscretizationInterface timeDiscretization, TimeDiscretizationInterface liborPeriodDiscretization, int numberOfFactors, double a) {
		super(timeDiscretization, liborPeriodDiscretization);

		this.numberOfFactors	= numberOfFactors;
		this.a					= a;
		this.isCalibrateable	= false;

		initialize(numberOfFactors, a);
	}

	@Override
    public double	getFactorLoading(int timeIndex, int factor, int component) {
		return factorMatrix[component][factor];
	}

	@Override
    public double	getCorrelation(int timeIndex, int component1, int component2) {
		return correlationMatrix[component1][component2];
	}

	@Override
    public int		getNumberOfFactors() {
		return factorMatrix[0].length;
	}

	private void initialize(int numberOfFactors, double a) {
		/*
		 * Create instantaneous correlation matrix
		 */
		correlationMatrix = new double[liborPeriodDiscretization.getNumberOfTimeSteps()][liborPeriodDiscretization.getNumberOfTimeSteps()];
		for(int row=0; row<correlationMatrix.length; row++) {
			for(int col=0; col<correlationMatrix[row].length; col++) {
				// Exponentially decreasing instantaneous correlation
				correlationMatrix[row][col] = Math.exp(-a * Math.abs(liborPeriodDiscretization.getTime(row)-liborPeriodDiscretization.getTime(col)));
			}
		}

		/*
		 * Perform a factor decomposition (and reduction if numberOfFactors < correlationMatrix.columns())
		 */
        factorMatrix = LinearAlgebra.factorReduction(correlationMatrix, numberOfFactors);

        for(int component1=0; component1<factorMatrix.length; component1++) {
            for(int component2=0; component2<component1; component2++) {
            	double correlation = 0.0;
            	for(int factor=0; factor<factorMatrix[component1].length; factor++) {
            		correlation += factorMatrix[component1][factor] * factorMatrix[component2][factor];
            	}
            	correlationMatrix[component1][component2] = correlation;
            	correlationMatrix[component2][component1] = correlation;
            }
        	correlationMatrix[component1][component1] = 1.0;
        }
	}

	@Override
	public double[] getParameter() {
		if(!isCalibrateable) return null;

		double[] parameter = new double[1];

		parameter[0] = a;

		return parameter;
	}
}
