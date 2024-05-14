package com.justbelieveinmyself.mocf;

import java.util.Arrays;

public class SimplexTableau {

    private double[][] tableau;
    private int[] basis;
    private int numDecisionVariables;
    private OptimizationGoal optimizationGoal;

    public SimplexTableau(double[][] coefficients, double[] bValues, double[] objectiveFunction, Constraint[] constraints, OptimizationGoal optimizationGoal) {
        int numRows = coefficients.length;
        int numCols = coefficients[0].length + numRows;
        tableau = new double[numRows + 1][numCols + 1];
        basis = new int[numRows];

        this.optimizationGoal = optimizationGoal;

        // Переход к КЗЛП
        for (int i = 0; i < numRows; i++) {
            if (constraints[i] == Constraint.LESS_THAN_OR_EQUAL_TO_ZERO) {
                // Добавляем новую переменную слагаемое
                double[] newRow = new double[numCols + 1];
                Arrays.fill(newRow, 0);
                newRow[numCols] = 1;
                tableau[i] = Arrays.copyOf(coefficients[i], numCols + 1);
                tableau[i][numCols] = 1; // Коэффициент новой переменной слагаемого
                tableau[i][numCols - 1] = bValues[i];
                basis[i] = numCols;
            } else {
                tableau[i] = Arrays.copyOf(coefficients[i], numCols);
                tableau[i][numCols - 1] = bValues[i];
                basis[i] = Arrays.asList(coefficients[i]).lastIndexOf(1);
            }
        }

        // Переход к СЗЛП
        for (int i = 0; i < numRows; i++) {
            if (constraints[i] == Constraint.GREATER_THAN_OR_EQUAL_TO_ZERO) {
                for (int j = 0; j < numCols; j++) {
                    tableau[i][j] *= -1;
                }
            }
        }

        numDecisionVariables = objectiveFunction.length;

        // Добавляем целевую функцию в таблицу
        for (int j = 0; j < numDecisionVariables; j++) {
            if (optimizationGoal == OptimizationGoal.MINIMIZE) {
                tableau[numRows][j] = -objectiveFunction[j]; // Минимизация: меняем знак целевой функции
            } else {
                tableau[numRows][j] = objectiveFunction[j]; // Максимизация: оставляем целевую функцию без изменений
            }
        }
    }

    public boolean isOptimal() {
        // Проверяем, все ли коэффициенты целевой функции неотрицательны
        for (int j = 0; j < tableau[0].length - 1; j++) {
            if (tableau[tableau.length - 1][j] < 0) {
                return false;
            }
        }
        return true;
    }

    public int getEnteringColumn() {
        // Находим разрешающий столбец
        int enteringColumn = -1;
        if (optimizationGoal == OptimizationGoal.MINIMIZE) {
            double minCoefficient = 0;
            for (int j = 0; j < tableau[0].length - 1; j++) {
                if (tableau[tableau.length - 1][j] < minCoefficient) {
                    minCoefficient = tableau[tableau.length - 1][j];
                    enteringColumn = j;
                }
            }
        } else {
            double maxCoefficient = 0;
            for (int j = 0; j < tableau[0].length - 1; j++) {
                if (tableau[tableau.length - 1][j] > maxCoefficient) {
                    maxCoefficient = tableau[tableau.length - 1][j];
                    enteringColumn = j;
                }
            }
        }
        return enteringColumn;
    }

    public int getExitingRow(int enteringColumn) {
        // Находим разрешающую строку
        int exitingRow = -1;
        double minRatio = Double.MAX_VALUE;
        for (int i = 0; i < tableau.length - 1; i++) {
            if (tableau[i][enteringColumn] > 0) {
                double ratio = tableau[i][tableau[0].length - 1] / tableau[i][enteringColumn];
                if (ratio < minRatio) {
                    minRatio = ratio;
                    exitingRow = i;
                }
            }
        }
        return exitingRow;
    }

    public void updateTableau(int enteringColumn, int exitingRow) {
        // Обновляем таблицу по правилам симплекс-метода
        double pivot = tableau[exitingRow][enteringColumn];
        for (int j = 0; j < tableau[0].length; j++) {
            tableau[exitingRow][j] /= pivot;
        }

    }}