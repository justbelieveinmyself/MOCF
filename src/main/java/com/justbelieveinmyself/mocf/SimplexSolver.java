package com.justbelieveinmyself.mocf;

import java.util.Arrays;

public class SimplexSolver {

    private double[][] coefficients;
    private double[] bValues;
    private Constraint[] constraints;
    private double[] objectiveFunction;
    private OptimizationGoal optimizationGoal;

    public SimplexSolver(double[][] coefficients, double[] bValues, Constraint[] constraints, double[] objectiveFunction, OptimizationGoal optimizationGoal) {
        this.coefficients = coefficients;
        this.bValues = bValues;
        this.constraints = constraints;
        this.objectiveFunction = objectiveFunction;
        this.optimizationGoal = optimizationGoal;
    }

    public double[] solve() {
        while (!isOptimal()) {
            int enteringVariable = findEnteringVariable();
            int exitingVariable = findExitingVariable(enteringVariable);

            if (exitingVariable == -1) {
                return null;
            }

            updateBasicSolution(enteringVariable, exitingVariable);
        }

        double[] result = new double[objectiveFunction.length];

        for (int i = 0; i < constraints.length; i++) {
            if (constraints[i] == Constraint.NO_CONSTRAINTS) {
                result[i] = 0;
            } else {
                boolean found = false;
                for (int j = 0; j < coefficients[i].length; j++) {
                    if (coefficients[i][j] == 1) {
                        result[j] = bValues[i];
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    result[i] = 0;
                }
            }
        }

        return result;
    }


    private boolean isOptimal() {
        double[] objectiveRow = coefficients[coefficients.length - 1];
        int lastColumnIndex = objectiveRow.length - 1;

        if (optimizationGoal == OptimizationGoal.MINIMIZE) {
            for (int j = 0; j < lastColumnIndex; j++) {
                if (objectiveRow[j] < 0) {
                    return false;
                }
            }
        } else if (optimizationGoal == OptimizationGoal.MAXIMIZE) {
            for (int j = 0; j < lastColumnIndex; j++) {
                if (objectiveRow[j] > 0) {
                    return false;
                }
            }
        }

        return true;
    }

    private int findEnteringVariable() {
        double[] objectiveRow = coefficients[coefficients.length - 1];
        int lastColumnIndex = objectiveRow.length - 1;
        int enteringVariable = -1;
        double maxCoefficient = 0;

        if (optimizationGoal == OptimizationGoal.MINIMIZE) {
            for (int j = 0; j < lastColumnIndex; j++) {
                if (objectiveRow[j] < maxCoefficient) {
                    maxCoefficient = objectiveRow[j];
                    enteringVariable = j;
                }
            }
        } else if (optimizationGoal == OptimizationGoal.MAXIMIZE) {
            for (int j = 0; j < lastColumnIndex; j++) {
                if (objectiveRow[j] > maxCoefficient) {
                    maxCoefficient = objectiveRow[j];
                    enteringVariable = j;
                }
            }
        }

        return enteringVariable;
    }

    private int findExitingVariable(int enteringVariable) {
        int exitingVariable = -1;
        int pivotRow = -1;
        double minRatio = Double.MAX_VALUE;

        for (int i = 0; i < coefficients.length - 1; i++) {
            double coefficient = coefficients[i][enteringVariable];
            if (coefficient > 0) {
                double ratio = bValues[i] / coefficient;
                if (ratio < minRatio) {
                    minRatio = ratio;
                    exitingVariable = i;
                    pivotRow = i;
                }
            }
        }

        if (pivotRow == -1) {
            return -1;
        }

        return exitingVariable;
    }

    private void updateBasicSolution(int enteringVariable, int exitingVariable) {
        double pivot = coefficients[exitingVariable][enteringVariable];
        double[] exitingRow = coefficients[exitingVariable];

        for (int j = 0; j < exitingRow.length; j++) {
            exitingRow[j] /= pivot;
        }

        for (int i = 0; i < coefficients.length; i++) {
            if (i != exitingVariable) {
                double[] currentRow = coefficients[i];
                double factor = currentRow[enteringVariable];
                for (int j = 0; j < currentRow.length; j++) {
                    currentRow[j] -= factor * exitingRow[j];
                }
            }
        }

        bValues[exitingVariable] /= pivot;
    }


}
