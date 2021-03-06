/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 26.11.2012
 */
package net.finmath.marketdata.calibration;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.finmath.marketdata.model.AnalyticModelInterface;
import net.finmath.marketdata.model.curves.CurveInterface;
import net.finmath.marketdata.products.AnalyticProductInterface;
import net.finmath.optimizer.LevenbergMarquardt;
import net.finmath.optimizer.SolverException;

/**
 * Generates a calibrated model for a given set
 * of <code>calibrationProducts</code> with respect to given <code>Curve</code>s.
 * 
 * The model and the curve are assumed to be immutable, i.e., the solver
 * will return a calibrate clone, containing clones for every curve
 * which is part of the set of curves to be calibrated.
 * 
 * The calibration is performed as a multi-threadded global optimization.
 * I will greatly profit from a multi-core architecture.
 * 
 * @author Christian Fries
 */
public class Solver {

	private final AnalyticModelInterface				model;
	private final List<AnalyticProductInterface> calibrationProducts;

	private int iterations		= 0;
	private final int maxIterations	= 10000;

	/**
	 * Generate a solver for the given parameter objects (independents) and
	 * objective functions (dependents).
	 * 
	 * @param model The model from which a calibrated clone should be created.
	 * @param calibrationProducts The objective functions.
	 */
    public Solver(AnalyticModelInterface model, Vector<AnalyticProductInterface> calibrationProducts) {
	    super();
	    this.model = model;
	    this.calibrationProducts = calibrationProducts;
    }

    /**
     * Find the model such that for the equation
     * objectiveFunctions.getValue(model) = 0
     * holds.
     * 
     * @return A reference to a calibrated clone of the given model.
     * @throws net.finmath.optimizer.SolverException
     */
    public AnalyticModelInterface getCalibratedModel(Set<CurveInterface> curvesToCalibrates) throws SolverException {
		final ParameterAggregation<CurveInterface> parameterAggregate = new ParameterAggregation<CurveInterface>(curvesToCalibrates);

		// Set solver parameters
		double[] initialParameters	= parameterAggregate.getParameter();
		double[] zeros	= new double[calibrationProducts.size()];
		java.util.Arrays.fill(zeros, 0.0);

		int maxThreads		= Math.min(Math.max(Runtime.getRuntime().availableProcessors(), 1), initialParameters.length);

		LevenbergMarquardt optimizer = new LevenbergMarquardt(
				initialParameters,
				zeros, /* targetValues */
				maxIterations,
				maxThreads)
		{	
			@Override
            public void setValues(double[] parameters, double[] values) throws SolverException {
				try {
					Map<CurveInterface, double[]> curvesParameterPairs = parameterAggregate.getObjectsToModifyForParameter(parameters);
					AnalyticModelInterface modelClone = model.getCloneForParameter(curvesParameterPairs);
					for(int i=0; i<calibrationProducts.size(); i++) {
						values[i] = calibrationProducts.get(i).getValue(modelClone);
					}
				} catch (CloneNotSupportedException e) {
					throw new SolverException(e);
				}
			}
		};
		optimizer.run();
		
		iterations = optimizer.getIterations();
	
		AnalyticModelInterface calibratedModel = null;
		try {
			double[] bestParameters = optimizer.getBestFitParameters();
			Map<CurveInterface, double[]> curvesParameterPairs = parameterAggregate.getObjectsToModifyForParameter(bestParameters);
			calibratedModel = model.getCloneForParameter(curvesParameterPairs);
		} catch (CloneNotSupportedException e) {
			throw new SolverException(e);
		}
		return calibratedModel;
    }
    	
    /**
     * Returns the number of iterations requited in the last solver step.
     * 
	 * @return The number of iterations required.
	 */
    public int getIterations() {
    	return iterations;
    }
}
