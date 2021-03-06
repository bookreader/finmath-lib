/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 09.02.2006
 */
package net.finmath.montecarlo;

import java.util.Arrays;

import net.finmath.stochastic.RandomVariableInterface;

import org.apache.commons.math3.util.FastMath;

/**
 * The class RandomVariable represents a random variable being the evaluation of a stochastic process
 * at a certain time within a Monte-Carlo simulation.
 * It is thus essentially a vector of doubles - the realizations - together with a double - the time.
 * The index of the vector represents path.
 * The class may also be used for non-stochastic quantities which may potentially be stochastic
 * (e.g. volatility). If only non-stochastic random variables are involved in an operation the class uses
 * optimized code.
 *
 * Accesses performed exclusively through the interface
 * <code>RandomVariableInterface</code> is thread safe (and does not mutate the class).
 *
 * @author Christian Fries
 * @version 1.8
 */
public class RandomVariable implements RandomVariableInterface {

    private double      time;	                // Time (filtration)

    // Data model for the stochastic case (otherwise null)
    private double[]    realizations;           // Realizations

    // Data model for the non-stochastic case (if realizations==null)
    private double      valueIfNonStochastic;

    /**
     * Create a non stochastic random variable, i.e. a constant.
     *
     * @param value the value, a constant.
     */
    public RandomVariable(double value) {
        this(0.0, value);
    }

    /**
     * Create a non stochastic random variable, i.e. a constant.
     *
     * @param time the filtration time, set to 0.0 if not used.
     * @param value the value, a constant.
     */
    public RandomVariable(double time, double value) {
        super();
        this.time = time;
        this.realizations = null;
        this.valueIfNonStochastic = value;
    }

    /**
     * Create a non stochastic random variable, i.e. a constant.
     *
     * @param time the filtration time, set to 0.0 if not used.
     * @param value the value, a constant.
     */
    public RandomVariable(double time, int numberOfPath, double value) {
        super();
        this.time = time;
        this.realizations = new double[numberOfPath];
        java.util.Arrays.fill(this.realizations, value);
        this.valueIfNonStochastic = Double.NaN;
    }

    /**
     * Create a stochastic random variable.
     *
     * @param time the filtration time, set to 0.0 if not used.
     * @param realisations the vector of realizations.
     */
    public RandomVariable(double time, double[] realisations) {
        super();
        this.time = time;
        this.realizations = realisations;
        this.valueIfNonStochastic = Double.NaN;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getMutableCopy()
     */
    public RandomVariable getMutableCopy() {
        return this;

        //if(isDeterministic())	return new RandomVariable(time, valueIfNonStochastic);
        //else					return new RandomVariable(time, realizations.clone());
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#equals(net.finmath.montecarlo.RandomVariable)
     */
    @Override
    public boolean equals(RandomVariableInterface randomVariable) {
        if(this.time != randomVariable.getFiltrationTime()) return false;
        if(this.isDeterministic() && randomVariable.isDeterministic()) {
            return this.valueIfNonStochastic == randomVariable.get(0);
        }

        if(this.isDeterministic() != randomVariable.isDeterministic()) return false;

        for(int i=0; i<realizations.length; i++) if(realizations[i] != randomVariable.get(i)) return false;

        return true;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getFiltrationTime()
     */
    @Override
    public double getFiltrationTime() {
        return time;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#get(int)
     */
    @Override
    public double get(int pathOrState) {
        if(isDeterministic())   return valueIfNonStochastic;
        else               		return realizations[pathOrState];
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#size()
     */
    @Override
    public int size() {
        if(isDeterministic())    return 1;
        else                     return realizations.length;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getMin()
     */
    public double getMin() {
        if(isDeterministic()) return valueIfNonStochastic;
        double min = Double.MAX_VALUE;
        if(realizations.length != 0) min = realizations[0];     /// @see getMax()
        for(int i=0; i<realizations.length; i++) min = Math.min(realizations[i],min);
        return min;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getMax()
     */
    @Override
    public double getMax() {
        if(isDeterministic()) return valueIfNonStochastic;
        double max = Double.MIN_VALUE;
        if(realizations.length != 0) max = realizations[0];     /// @bug Workaround. There seems to be a bug in Java 1.4 with Math.max(Double.MIN_VALUE,0.0)
        for(int i=0; i<realizations.length; i++) max = Math.max(realizations[i],max);
        return max;
    }

    /**
     * @return Sum of all realizations.
     */
    public double getSum() {
        if(isDeterministic())	return valueIfNonStochastic;
        if(size() == 0)			return Double.NaN;

        double sum = 0.0;
        for(int i=0; i<realizations.length; i++) sum += realizations[i];
        return sum;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getAverage()
     */
    public double getAverage() {
        if(isDeterministic())	return valueIfNonStochastic;
        if(size() == 0)			return Double.NaN;

        double sum = 0.0;
        for(int i=0; i<realizations.length; i++) sum += realizations[i];
        return sum/realizations.length;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getAverage(net.finmath.stochastic.RandomVariableInterface)
     */
    @Override
    public double getAverage(RandomVariableInterface probabilities) {
        if(isDeterministic())	return valueIfNonStochastic;
        if(size() == 0)			return Double.NaN;

        double average = 0.0;
        for(int i=0; i<realizations.length; i++) average += realizations[i] * probabilities.get(i);
        return average;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getVariance()
     */
    @Override
    public double getVariance() {
        if(isDeterministic())	return 0.0;
        if(size() == 0)			return Double.NaN;

        double sum			= 0.0;
        double sumOfSquared = 0.0;
        for(double realization : realizations) {
            sum				+= realization;
            sumOfSquared	+= realization * realization;
        }
        return sumOfSquared/realizations.length - sum/realizations.length * sum/realizations.length;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getVariance(net.finmath.stochastic.RandomVariableInterface)
     */
    public double getVariance(RandomVariableInterface probabilities) {
        if(isDeterministic())	return 0.0;
        if(size() == 0)			return Double.NaN;

        double mean			= 0.0;
        double secondMoment = 0.0;
        for(int i=0; i<realizations.length; i++) {
            mean			+= realizations[i] * probabilities.get(i);
            secondMoment	+= realizations[i] * realizations[i] * probabilities.get(i);
        }
        return secondMoment - mean*mean;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getStandardDeviation()
     */
    @Override
    public double getStandardDeviation() {
        if(isDeterministic())	return 0.0;
        if(size() == 0)			return Double.NaN;

        return Math.sqrt(getVariance());
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getStandardDeviation(net.finmath.stochastic.RandomVariableInterface)
     */
    @Override
    public double getStandardDeviation(RandomVariableInterface probabilities) {
        if(isDeterministic())	return 0.0;
        if(size() == 0)			return Double.NaN;

        return Math.sqrt(getVariance(probabilities));
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getStandardError()
     */
    @Override
    public double getStandardError() {
        if(isDeterministic())	return 0.0;
        if(size() == 0)			return Double.NaN;

        return getStandardDeviation()/Math.sqrt(size());
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getStandardError(net.finmath.stochastic.RandomVariableInterface)
     */
    @Override
    public double getStandardError(RandomVariableInterface probabilities) {
        if(isDeterministic())	return 0.0;
        if(size() == 0)			return Double.NaN;

        return getStandardDeviation(probabilities)/Math.sqrt(size());
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getQuantile()
     */
    @Override
    public double getQuantile(double quantile) {
        if(isDeterministic())	return valueIfNonStochastic;
        if(size() == 0)			return Double.NaN;

        double[] realizationsSorted = realizations.clone();
        java.util.Arrays.sort(realizationsSorted);

        int indexOfQuantileValue = Math.min(Math.max((int)Math.round((size()+1) * (1-quantile) - 1), 0), size()-1);

        return realizationsSorted[indexOfQuantileValue];
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getQuantile(net.finmath.stochastic.RandomVariableInterface)
     */
    public double getQuantile(double quantile, RandomVariableInterface probabilities) {
        if(isDeterministic())	return valueIfNonStochastic;
        if(size() == 0)			return Double.NaN;

        throw new RuntimeException("Method not implemented.");
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getConditionalVaR()
     */
    @Override
    public double getQuantileExpectation(double quantileStart, double quantileEnd) {
        if(isDeterministic())	return valueIfNonStochastic;
        if(size() == 0)			return Double.NaN;
        if(quantileStart > quantileEnd) return getQuantileExpectation(quantileEnd, quantileStart);

        double[] realizationsSorted = realizations.clone();
        java.util.Arrays.sort(realizationsSorted);

        int indexOfQuantileValueStart	= Math.min(Math.max((int)Math.round((size()+1) * quantileStart - 1), 0), size()-1);
        int indexOfQuantileValueEnd		= Math.min(Math.max((int)Math.round((size()+1) * quantileEnd - 1), 0), size()-1);

        double quantileExpectation = 0.0;
        for (int i=indexOfQuantileValueStart; i<=indexOfQuantileValueEnd;i++) {
            quantileExpectation += realizationsSorted[i];
        }
        quantileExpectation /= indexOfQuantileValueEnd-indexOfQuantileValueStart+1;

        return quantileExpectation;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getHistogram()
     */
    @Override
    public double[] getHistogram(double[] intervalPoints)
    {
        double[] histogramValues = new double[intervalPoints.length+1];

        if(isDeterministic()) {
			/*
			 * If the random variable is deterministic we will return an array
			 * consisting of 0's and one and only one 1.
			 */
            java.util.Arrays.fill(histogramValues, 0.0);
            for (int intervalIndex=0; intervalIndex<intervalPoints.length; intervalIndex++)
            {
                if(valueIfNonStochastic > intervalPoints[intervalIndex]) {
                    histogramValues[intervalIndex] = 1.0;
                    break;
                }
            }
            histogramValues[intervalPoints.length] = 1.0;
        }
        else {
			/*
			 * If the random variable is deterministic we will return an array
			 * representing a density, where the sum of the entries is one.
			 * There is one exception:
			 * If the size of the random variable is 0, all entries will be zero.
			 */
            double[] realizationsSorted = realizations.clone();
            java.util.Arrays.sort(realizationsSorted);

            int sampleIndex=0;
            for (int intervalIndex=0; intervalIndex<intervalPoints.length; intervalIndex++)
            {
                int sampleCount = 0;
                while (sampleIndex < realizationsSorted.length &&
                        realizationsSorted[sampleIndex] <= intervalPoints[intervalIndex])
                {
                    sampleIndex++;
                    sampleCount++;
                }
                histogramValues[intervalIndex] = sampleCount;
            }
            histogramValues[intervalPoints.length] = realizationsSorted.length-sampleIndex;

            // Normalize histogramValues
            if(realizationsSorted.length > 0) {
                for(int i=0; i<histogramValues.length; i++) histogramValues[i] /= realizationsSorted.length;
            }
        }

        return histogramValues;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getHistogram(int,double)
     */
    @Override
    public double[][] getHistogram(int numberOfPoints, double standardDeviations) {
        double[] intervalPoints = new double[numberOfPoints];
        double[] anchorPoints	= new double[numberOfPoints+1];
        double center	= getAverage();
        double radius	= standardDeviations * getStandardDeviation();
        double stepSize	= (double) (numberOfPoints-1) / 2.0;
        for(int i=0; i<numberOfPoints;i++) {
            double alpha = (-(double)(numberOfPoints-1) / 2.0 + (double)i) / stepSize;
            intervalPoints[i]	= center + alpha * radius;
            anchorPoints[i]		= center + alpha * radius - radius / (2 * stepSize);
        }
        anchorPoints[numberOfPoints] = center + 1 * radius + radius / (2 * stepSize);

        double[][] result = new double[2][];
        result[0] = anchorPoints;
        result[1] = getHistogram(intervalPoints);

        return result;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#isDeterministic()
     */
    @Override
    public boolean isDeterministic() {
        return realizations == null;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#expand()
     */
    public void expand(int numberOfPaths) {
        if(isDeterministic()) {
            // Expand random variable to a vector of path values
            realizations = new double[numberOfPaths];
            java.util.Arrays.fill(realizations,valueIfNonStochastic);
        }
        return;
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#getRealizations()
     */
    public double[] getRealizations() {
        if(isDeterministic()) {
            double[] result = new double[1];
            result[0] = get(0);
            return result;
        }
        else {
            return realizations.clone();
        }
    }

    /**
     * Returns the realizations as double array. If the random variable is deterministic, then it is expanded
     * to the given number of paths.
     *
     * @param numberOfPaths Number of paths.
     * @return The realization as double array.
     */
    public double[] getRealizations(int numberOfPaths) {

        if(!isDeterministic() && realizations.length != numberOfPaths) throw new RuntimeException("Inconsistent number of paths.");
        this.expand(numberOfPaths);

        return realizations;//.clone();
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#cap(double)
     */
    public RandomVariableInterface cap(double cap) {
        if(isDeterministic()) {
            double newValueIfNonStochastic = Math.min(valueIfNonStochastic,cap);
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = Math.min(realizations[i],cap);
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#floor(double)
     */
    @Override
    public RandomVariableInterface floor(double floor) {
        if(isDeterministic()) {
            double newValueIfNonStochastic = Math.max(valueIfNonStochastic,floor);
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = Math.max(realizations[i],floor);
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#add(double)
     */
    @Override
    public RandomVariableInterface add(double value) {
        if(isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic + value;
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] + value;
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#sub(double)
     */
    @Override
    public RandomVariableInterface sub(double value) {
        if(isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic - value;
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] - value;
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#mult(double)
     */
    @Override
    public RandomVariableInterface mult(double value) {
        if(isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic * value;
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] * value;
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#div(double)
     */
    @Override
    public RandomVariableInterface div(double value) {
        if(isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic / value;
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] / value;
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#pow(double)
     */
    @Override
    public RandomVariableInterface pow(double exponent) {
        if(isDeterministic()) {
            double newValueIfNonStochastic = Math.pow(valueIfNonStochastic,exponent);
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = Math.pow(realizations[i],exponent);
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#squared()
     */
    @Override
    public RandomVariableInterface squared() {
        if(isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic * valueIfNonStochastic;
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] * realizations[i];
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#sqrt()
     */
    @Override
    public RandomVariableInterface sqrt() {
        if(isDeterministic()) {
            double newValueIfNonStochastic = Math.sqrt(valueIfNonStochastic);
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = Math.sqrt(realizations[i]);
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#exp()
     */
    public RandomVariable exp() {
        if(isDeterministic()) {
            double newValueIfNonStochastic = FastMath.exp(valueIfNonStochastic);
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = FastMath.exp(realizations[i]);
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#log()
     */
    public RandomVariable log() {
        if(isDeterministic()) {
            double newValueIfNonStochastic = FastMath.log(valueIfNonStochastic);
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = FastMath.log(realizations[i]);
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#sin()
     */
    public RandomVariableInterface sin() {
        if(isDeterministic()) {
            double newValueIfNonStochastic = FastMath.sin(valueIfNonStochastic);
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = FastMath.sin(realizations[i]);
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#cos()
     */
    public RandomVariableInterface cos() {
        if(isDeterministic()) {
            double newValueIfNonStochastic = FastMath.cos(valueIfNonStochastic);
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = FastMath.cos(realizations[i]);
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#add(net.finmath.stochastic.RandomVariableInterface)
     */
    public RandomVariableInterface add(RandomVariableInterface randomVariable) {
        // Set time of this random variable to maximum of time with respect to which measurability is known.
        double newTime = Math.max(time, randomVariable.getFiltrationTime());

        if(isDeterministic() && randomVariable.isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic + randomVariable.get(0);
            return new RandomVariable(newTime, newValueIfNonStochastic);
        }
        else if(isDeterministic()) return (randomVariable.getMutableCopy()).add(this);
        else {
            double[] newRealizations = new double[Math.max(size(), randomVariable.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] + randomVariable.get(i);
            return new RandomVariable(newTime, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#sub(net.finmath.stochastic.RandomVariableInterface)
     */
    public RandomVariableInterface sub(RandomVariableInterface randomVariable) {
        // Set time of this random variable to maximum of time with respect to which measurability is known.
        double newTime = Math.max(time, randomVariable.getFiltrationTime());

        if(isDeterministic() && randomVariable.isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic - randomVariable.get(0);
            return new RandomVariable(newTime, newValueIfNonStochastic);
        }
        else if(isDeterministic()) return ((RandomVariable)randomVariable).sub(this);
        else {
            double[] newRealizations = new double[Math.max(size(), randomVariable.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] - randomVariable.get(i);
            return new RandomVariable(newTime, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#mult(net.finmath.stochastic.RandomVariableInterface)
     */
    public RandomVariable mult(RandomVariableInterface randomVariable) {
        // Set time of this random variable to maximum of time with respect to which measurability is known.
        double newTime = Math.max(time, randomVariable.getFiltrationTime());

        if(isDeterministic() && randomVariable.isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic * randomVariable.get(0);
            return new RandomVariable(newTime, newValueIfNonStochastic);
        }
        else if(isDeterministic()) return ((RandomVariable)randomVariable).mult(this);
        else {
            double[] newRealizations = new double[Math.max(size(), randomVariable.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] * randomVariable.get(i);
            return new RandomVariable(newTime, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#div(net.finmath.stochastic.RandomVariableInterface)
     */
    public RandomVariable div(RandomVariableInterface randomVariable) {
        // Set time of this random variable to maximum of time with respect to which measurability is known.
        double newTime = Math.max(time, randomVariable.getFiltrationTime());

        if(isDeterministic() && randomVariable.isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic / randomVariable.get(0);
            return new RandomVariable(newTime, newValueIfNonStochastic);
        }
        else if(isDeterministic()) {
            double[] newRealizations = new double[Math.max(size(), randomVariable.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = valueIfNonStochastic / randomVariable.get(i);
            return new RandomVariable(newTime, newRealizations);
        }
        else {
            double[] newRealizations = new double[Math.max(size(), randomVariable.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] / randomVariable.get(i);
            return new RandomVariable(newTime, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#cap(net.finmath.stochastic.RandomVariableInterface)
     */
    public RandomVariableInterface cap(RandomVariableInterface randomVariable) {
        // Set time of this random variable to maximum of time with respect to which measurability is known.
        double newTime = Math.max(time, randomVariable.getFiltrationTime());

        if(isDeterministic() && randomVariable.isDeterministic()) {
            double newValueIfNonStochastic = FastMath.min(valueIfNonStochastic, randomVariable.get(0));
            return new RandomVariable(newTime, newValueIfNonStochastic);
        }
        else if(isDeterministic()) return ((RandomVariable)randomVariable).cap(this);
        else {
            double[] newRealizations = new double[Math.max(size(), randomVariable.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = FastMath.min(realizations[i], randomVariable.get(i));
            return new RandomVariable(newTime, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#floor(net.finmath.stochastic.RandomVariableInterface)
     */
    public RandomVariableInterface floor(RandomVariableInterface randomVariable) {
        // Set time of this random variable to maximum of time with respect to which measurability is known.
        double newTime = Math.max(time, randomVariable.getFiltrationTime());

        if(isDeterministic() && randomVariable.isDeterministic()) {
            double newValueIfNonStochastic = FastMath.max(valueIfNonStochastic, randomVariable.get(0));
            return new RandomVariable(newTime, newValueIfNonStochastic);
        }
        else if(isDeterministic()) return ((RandomVariable)randomVariable).floor(this);
        else {
            double[] newRealizations = new double[Math.max(size(), randomVariable.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = FastMath.max(realizations[i], randomVariable.get(i));
            return new RandomVariable(newTime, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#accrue(net.finmath.stochastic.RandomVariableInterface, double)
     */
    public RandomVariableInterface accrue(RandomVariableInterface rate, double periodLength) {
        // Set time of this random variable to maximum of time with respect to which measurability is known.
        double newTime = Math.max(time, rate.getFiltrationTime());

        if(isDeterministic() && rate.isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic * (1 + rate.get(0) * periodLength);
            return new RandomVariable(newTime, newValueIfNonStochastic);
        }
        else if(isDeterministic() && !rate.isDeterministic()) {
            double[] rateRealizations = rate.getRealizations();
            double[] newRealizations = new double[Math.max(size(), rate.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = valueIfNonStochastic * (1 + rateRealizations[i] * periodLength);
            return new RandomVariable(newTime, newRealizations);
        }
        else if(!isDeterministic() && rate.isDeterministic()) {
            double rateValue = rate.get(0);
            double[] newRealizations = new double[Math.max(size(), rate.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] * (1 + rateValue * periodLength);
            return new RandomVariable(newTime, newRealizations);
        }
        else {
            double[] rateRealizations = rate.getRealizations();
            double[] newRealizations = new double[Math.max(size(), rate.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] * (1 + rateRealizations[i] * periodLength);
            return new RandomVariable(newTime, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#discount(net.finmath.stochastic.RandomVariableInterface, double)
     */
    public RandomVariableInterface discount(RandomVariableInterface rate, double periodLength) {
        // Set time of this random variable to maximum of time with respect to which measurability is known.
        double newTime = Math.max(time, rate.getFiltrationTime());

        if(isDeterministic() && rate.isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic / (1 + rate.get(0) * periodLength);
            return new RandomVariable(newTime, newValueIfNonStochastic);
        }
        else if(isDeterministic() && !rate.isDeterministic()) {
            double[] rateRealizations = rate.getRealizations();
            double[] newRealizations = new double[Math.max(size(), rate.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = valueIfNonStochastic / (1.0 + rateRealizations[i] * periodLength);
            return new RandomVariable(newTime, newRealizations);
        }
        else if(!isDeterministic() && rate.isDeterministic()) {
            double rateValue = rate.get(0);
            double[] newRealizations = new double[Math.max(size(), rate.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] / (1.0 + rateValue * periodLength);
            return new RandomVariable(newTime, newRealizations);
        }
        else {
            double[] rateRealizations = rate.getRealizations();
            double[] newRealizations = new double[Math.max(size(), rate.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] / (1.0 + rateRealizations[i] * periodLength);
            return new RandomVariable(newTime, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#barrier(net.finmath.stochastic.RandomVariableInterface, net.finmath.stochastic.RandomVariableInterface, net.finmath.stochastic.RandomVariableInterface)
     */
    public RandomVariableInterface barrier(RandomVariableInterface trigger, RandomVariableInterface valueIfTriggerNonNegative, RandomVariableInterface valueIfTriggerNegative) {
        // Set time of this random variable to maximum of time with respect to which measurability is known.
        double newTime = Math.max(time, trigger.getFiltrationTime());
        newTime = Math.max(newTime, valueIfTriggerNonNegative.getFiltrationTime());
        newTime = Math.max(newTime, valueIfTriggerNegative.getFiltrationTime());

        if(isDeterministic() && trigger.isDeterministic() && valueIfTriggerNonNegative.isDeterministic() && valueIfTriggerNegative.isDeterministic()) {
            double newValueIfNonStochastic = trigger.get(0) >= 0 ? valueIfTriggerNonNegative.get(0) : valueIfTriggerNegative.get(0);
            return new RandomVariable(newTime, newValueIfNonStochastic);
        }
        else {
            int numberOfPaths = Math.max(Math.max(trigger.size(), valueIfTriggerNonNegative.size()), valueIfTriggerNegative.size());
            double[] newRealizations = new double[numberOfPaths];
            for(int i=0; i<newRealizations.length; i++) {
                newRealizations[i] = trigger.get(i) >= 0.0 ? valueIfTriggerNonNegative.get(i) : valueIfTriggerNegative.get(i);
            }
            return new RandomVariable(newTime, newRealizations);
        }
    }

    public RandomVariableInterface barrier(RandomVariableInterface trigger, RandomVariableInterface valueIfTriggerNonNegative, double valueIfTriggerNegative) {
        return this.barrier(trigger, valueIfTriggerNonNegative, new RandomVariable(valueIfTriggerNonNegative.getFiltrationTime(), valueIfTriggerNegative));
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#invert()
     */
    public RandomVariableInterface invert() {
        if(isDeterministic()) {
            double newValueIfNonStochastic = 1.0/valueIfNonStochastic;
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = 1.0/realizations[i];
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#abs()
     */
    public RandomVariableInterface abs() {
        if(isDeterministic()) {
            double newValueIfNonStochastic = Math.abs(valueIfNonStochastic);
            return new RandomVariable(time, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[realizations.length];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = Math.abs(realizations[i]);
            return new RandomVariable(time, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#addProduct(net.finmath.stochastic.RandomVariableInterface, double)
     */
    public RandomVariableInterface addProduct(RandomVariableInterface factor1, double factor2) {
        // Set time of this random variable to maximum of time with respect to which measurability is known.
        double newTime = Math.max(time, factor1.getFiltrationTime());

        if(isDeterministic() && factor1.isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic + (factor1.get(0) * factor2);
            return new RandomVariable(newTime, newValueIfNonStochastic);
        }
        else if(isDeterministic() && !factor1.isDeterministic()) {
            double[] factor1Realizations = factor1.getRealizations();
            double[] newRealizations = new double[Math.max(size(), factor1.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = valueIfNonStochastic + factor1Realizations[i] * factor2;
            return new RandomVariable(newTime, newRealizations);
        }
        else if(!isDeterministic() && factor1.isDeterministic()) {
            double factor1Value = factor1.get(0);
            double[] newRealizations = new double[Math.max(size(), factor1.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] + factor1Value * factor2;
            return new RandomVariable(newTime, newRealizations);
        }
        else {
            double[] factor1Realizations = factor1.getRealizations();
            double[] newRealizations = new double[Math.max(size(), factor1.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] + factor1Realizations[i] * factor2;
            return new RandomVariable(newTime, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#addProduct(net.finmath.stochastic.RandomVariableInterface, net.finmath.stochastic.RandomVariableInterface)
     */
    public RandomVariableInterface addProduct(RandomVariableInterface factor1, RandomVariableInterface factor2) {
        // Set time of this random variable to maximum of time with respect to which measurability is known.
        double newTime = Math.max(Math.max(time, factor1.getFiltrationTime()), factor2.getFiltrationTime());

        if(isDeterministic() && factor1.isDeterministic() && factor2.isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic + (factor1.get(0) * factor2.get(0));
            return new RandomVariable(newTime, newValueIfNonStochastic);
        }
        else if(isDeterministic() && !factor1.isDeterministic() && !factor2.isDeterministic()) {
            double[] factor1Realizations = factor1.getRealizations();
            double[] factor2Realizations = factor2.getRealizations();
            double[] newRealizations = new double[Math.max(size(), factor1.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = valueIfNonStochastic + factor1Realizations[i] * factor2Realizations[i];
            return new RandomVariable(newTime, newRealizations);
        }
        else if(!isDeterministic() && !factor1.isDeterministic() && !factor2.isDeterministic()) {
            double[] factor1Realizations = factor1.getRealizations();
            double[] factor2Realizations = factor2.getRealizations();
            double[] newRealizations = new double[Math.max(size(), factor1.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = realizations[i] + factor1Realizations[i] * factor2Realizations[i];
            return new RandomVariable(newTime, newRealizations);
        }
        else {
            double[] newRealizations = new double[Math.max(Math.max(size(), factor1.size()), factor2.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = get(i) + factor1.get(i) * factor2.get(i);
            return new RandomVariable(newTime, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#addRatio(net.finmath.stochastic.RandomVariableInterface, net.finmath.stochastic.RandomVariableInterface)
     */
    public RandomVariableInterface addRatio(RandomVariableInterface numerator, RandomVariableInterface denominator) {
        // Set time of this random variable to maximum of time with respect to which measurability is known.
        double newTime = Math.max(Math.max(time, numerator.getFiltrationTime()), denominator.getFiltrationTime());

        if(isDeterministic() && numerator.isDeterministic() && denominator.isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic + (numerator.get(0) / denominator.get(0));
            return new RandomVariable(newTime, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[Math.max(Math.max(size(), numerator.size()), denominator.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = get(i) + numerator.get(i) / denominator.get(i);
            return new RandomVariable(newTime, newRealizations);
        }
    }

    /* (non-Javadoc)
     * @see net.finmath.stochastic.RandomVariableInterface#subRatio(net.finmath.stochastic.RandomVariableInterface, net.finmath.stochastic.RandomVariableInterface)
     */
    public RandomVariableInterface subRatio(RandomVariableInterface numerator, RandomVariableInterface denominator) {
        // Set time of this random variable to maximum of time with respect to which measurability is known.
        double newTime = Math.max(Math.max(time, numerator.getFiltrationTime()), denominator.getFiltrationTime());

        if(isDeterministic() && numerator.isDeterministic() && denominator.isDeterministic()) {
            double newValueIfNonStochastic = valueIfNonStochastic - (numerator.get(0) / denominator.get(0));
            return new RandomVariable(newTime, newValueIfNonStochastic);
        }
        else {
            double[] newRealizations = new double[Math.max(Math.max(size(), numerator.size()), denominator.size())];
            for(int i=0; i<newRealizations.length; i++) newRealizations[i]		 = get(i) - numerator.get(i) / denominator.get(i);
            return new RandomVariable(newTime, newRealizations);
        }
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return super.toString()
				+ "\n" + "time: " + time
				+ "\n" + "realizations: " + Arrays.toString(realizations);
	}
}
