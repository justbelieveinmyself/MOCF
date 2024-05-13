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

    public void convertToStandardForm() {
        int slackVariablesCount = 0;
        for (Constraint constraint : constraints) {
            if (constraint == Constraint.LESS_THAN_OR_EQUAL_TO_ZERO || constraint == Constraint.GREATER_THAN_OR_EQUAL_TO_ZERO) {
                slackVariablesCount++;
            }
        }

        int newColumns = coefficients[0].length + slackVariablesCount;
        double[][] newCoefficients = new double[coefficients.length][newColumns];
        double[] newBValues = Arrays.copyOf(bValues, bValues.length);

        for (int i = 0; i < coefficients.length; i++) {
            System.arraycopy(coefficients[i], 0, newCoefficients[i], 0, coefficients[i].length);
            for (int j = coefficients[i].length; j < newColumns; j++) {
                if (j - coefficients[i].length == i && (constraints[i] == Constraint.LESS_THAN_OR_EQUAL_TO_ZERO || constraints[i] == Constraint.GREATER_THAN_OR_EQUAL_TO_ZERO)) {
                    newCoefficients[i][j] = 1;
                } else {
                    newCoefficients[i][j] = 0;
                }
            }
        }

        coefficients = newCoefficients;
        bValues = newBValues;

        if (optimizationGoal == OptimizationGoal.MINIMIZE) {
            for (int i = 0; i < objectiveFunction.length; i++) {
                objectiveFunction[i] *= -1;
            }
        }
    }



    public void solve() {
        convertToStandardForm();

        while (true) {
            if (isOptimal()) {
                break;
            }

            int pivotColumn = findPivotColumn();
            if (pivotColumn == -1) {
                System.out.println("Задача не ограничена");
                return;
            }

            int pivotRow = findPivotRow(pivotColumn);

            if (pivotRow == -1) {
                System.out.println("Задача не ограничена");
                return;
            }

            updateTable(pivotRow, pivotColumn);
        }

        printSolution();
    }


    private boolean isOptimal() {
        boolean isOptimal = true;
        for (double coefficient : objectiveFunction) {
            if (coefficient > 0) {
                isOptimal = false;
                break;
            }
        }
        return isOptimal;
    }



    private int findPivotColumn() {
        int pivotColumn = -1;
        double maxCoefficient = optimizationGoal == OptimizationGoal.MINIMIZE ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        for (int i = 0; i < objectiveFunction.length; i++) {
            if ((optimizationGoal == OptimizationGoal.MINIMIZE && objectiveFunction[i] < maxCoefficient) ||
                    (optimizationGoal == OptimizationGoal.MAXIMIZE && objectiveFunction[i] > maxCoefficient)) {
                maxCoefficient = objectiveFunction[i];
                pivotColumn = i;
            }
        }
        return pivotColumn;
    }


    private int findPivotRow(int pivotColumn) {
        int pivotRow = -1;
        double minRatio = Double.MAX_VALUE;
        for (int i = 0; i < bValues.length; i++) {
            if (coefficients[i][pivotColumn] > 0) {
                double ratio = bValues[i] / coefficients[i][pivotColumn];
                if (ratio < minRatio) {
                    minRatio = ratio;
                    pivotRow = i;
                }
            }
        }
        return pivotRow;
    }

    private void updateTable(int pivotRow, int pivotColumn) {
        double pivotElement = coefficients[pivotRow][pivotColumn];
        double[][] newCoefficients = new double[coefficients.length][coefficients[0].length];
        double[] newBValues = new double[bValues.length];

        for (int i = 0; i < coefficients.length; i++) {
            for (int j = 0; j < coefficients[i].length; j++) {
                if (i != pivotRow && j != pivotColumn) {
                    newCoefficients[i][j] = coefficients[i][j] - (coefficients[i][pivotColumn] * coefficients[pivotRow][j]) / pivotElement;
                }
            }
        }

        for (int i = 0; i < coefficients.length; i++) {
            if (i != pivotRow) {
                newCoefficients[i][pivotColumn] = 0;
            }
        }

        for (int j = 0; j < coefficients[pivotRow].length; j++) {
            if (j != pivotColumn) {
                newCoefficients[pivotRow][j] = coefficients[pivotRow][j] / pivotElement;
            }
        }
        newCoefficients[pivotRow][pivotColumn] = 1;
        newBValues[pivotRow] = bValues[pivotRow] / pivotElement;

        for (int i = 0; i < coefficients.length; i++) {
            if (i != pivotRow) {
                newBValues[i] = bValues[i] - (coefficients[i][pivotColumn] * bValues[pivotRow]) / pivotElement;
            }
        }

        coefficients = newCoefficients;
        bValues = newBValues;

        double[] newObjectiveFunction = new double[objectiveFunction.length];
        for (int i = 0; i < objectiveFunction.length; i++) {
            if (i != pivotColumn) {
                newObjectiveFunction[i] = objectiveFunction[i] - (objectiveFunction[pivotColumn] * coefficients[pivotRow][i]) / pivotElement;
            }
        }
        newObjectiveFunction[pivotColumn] = objectiveFunction[pivotColumn] / pivotElement;

        objectiveFunction = newObjectiveFunction;
    }


    private void printSolution() {
        System.out.println("Оптимальное решение:");
        for (int i = 0; i < objectiveFunction.length; i++) {
            System.out.println("x" + (i + 1) + " = " + (i < coefficients[0].length ? bValues[i] : 0));
        }
        double objectiveValue = optimizationGoal == OptimizationGoal.MINIMIZE ? -objectiveFunction[objectiveFunction.length - 1] : objectiveFunction[objectiveFunction.length - 1];
        System.out.println("Значение целевой функции: " + objectiveValue);
    }

    public static void main(String[] args) {
        double[][] coefficients = {{4, 1}, {1, -1}};
        double[] bValues = {8, -3};
        Constraint[] constraints = {Constraint.LESS_THAN_OR_EQUAL_TO_ZERO, Constraint.GREATER_THAN_OR_EQUAL_TO_ZERO};
        double[] objectiveFunction = {3, 4};
        SimplexSolver solver = new SimplexSolver(coefficients, bValues, constraints, objectiveFunction, OptimizationGoal.MAXIMIZE);
        solver.solve();
    }
}
