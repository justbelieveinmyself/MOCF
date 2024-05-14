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


    private void toStandardForm() {
        // Добавление искусственных переменных для неравенств
        int numSlackVariables = 0;
        for (Constraint constraint : constraints) {
            if (constraint == Constraint.GREATER_THAN_OR_EQUAL_TO_ZERO) {
                numSlackVariables++;
            }
        }

        int numVariables = coefficients[0].length;
        double[][] newCoefficients = new double[coefficients.length][numVariables + numSlackVariables];
        for (int i = 0; i < coefficients.length; i++) {
            System.arraycopy(coefficients[i], 0, newCoefficients[i], 0, coefficients[i].length);
        }

        int slackVarIndex = numVariables;
        for (int i = 0; i < constraints.length; i++) {
            if (constraints[i] == Constraint.GREATER_THAN_OR_EQUAL_TO_ZERO) {
                newCoefficients[i][slackVarIndex] = 1;
                slackVarIndex++;
            }
        }

        coefficients = newCoefficients;

        // Обновление целевой функции для искусственных переменных
        double[] newObjectiveFunction = Arrays.copyOf(objectiveFunction, numVariables + numSlackVariables);
        objectiveFunction = newObjectiveFunction;
        Arrays.fill(objectiveFunction, numVariables, numVariables + numSlackVariables, 0);

        // Обновление базиса
        for (int i = numVariables; i < numVariables + numSlackVariables; i++) {
            objectiveFunction[i] = -1;
        }
    }
    public void solve() {
        // Преобразование в КЗЛП
        toStandardForm();

        // Применение симплекс-метода
        while (!isOptimal()) {
            // Определение новой базисной переменной
            int pivotColumn = getPivotColumn();
            if (pivotColumn == -1) {
                System.out.println("Решение не существует.");
                return;
            }

            // Определение ведущей строки
            int pivotRow = getPivotRow(pivotColumn);
            if (pivotRow == -1) {
                System.out.println("Функция неограничена.");
                return;
            }

            // Обновление базиса
            updateBasis(pivotRow, pivotColumn);
        }

        // Вывод результатов
        printSolution();
    }

    private boolean isOptimal() {
        if (optimizationGoal == OptimizationGoal.MAXIMIZE) {
            for (double coefficient : objectiveFunction) {
                if (coefficient < 0) {
                    return false;
                }
            }
        } else if (optimizationGoal == OptimizationGoal.MINIMIZE) {
            for (double coefficient : objectiveFunction) {
                if (coefficient > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private int getPivotColumn() {
        if (optimizationGoal == OptimizationGoal.MAXIMIZE) {
            for (int i = 0; i < objectiveFunction.length; i++) {
                if (objectiveFunction[i] < 0) {
                    return i;
                }
            }
        } else if (optimizationGoal == OptimizationGoal.MINIMIZE) {
            for (int i = 0; i < objectiveFunction.length; i++) {
                if (objectiveFunction[i] > 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getPivotRow(int pivotColumn) {
        int pivotRow = -1;
        double minRatio = Double.MAX_VALUE;

        for (int i = 0; i < coefficients.length; i++) {
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

    private void updateBasis(int pivotRow, int pivotColumn) {
        // Обновление базисных переменных
        double pivotValue = coefficients[pivotRow][pivotColumn];
        for (int i = 0; i < coefficients[pivotRow].length; i++) {
            coefficients[pivotRow][i] /= pivotValue;
        }
        bValues[pivotRow] /= pivotValue;

        // Обновление остальных уравнений
        for (int i = 0; i < coefficients.length; i++) {
            if (i != pivotRow) {
                double factor = coefficients[i][pivotColumn];
                for (int j = 0; j < coefficients[i].length; j++) {
                    coefficients[i][j] -= factor * coefficients[pivotRow][j];
                }
                bValues[i] -= factor * bValues[pivotRow];
            }
        }

        // Обновление целевой функции
        double factor = objectiveFunction[pivotColumn];
        for (int i = 0; i < objectiveFunction.length; i++) {
            objectiveFunction[i] -= factor * coefficients[pivotRow][i];
        }
    }

    private void printSolution() {
        System.out.println("Оптимальное решение найдено:");
        System.out.println("Значения переменных:");
        for (int i = 0; i < coefficients[0].length; i++) {
            boolean isBasic = false;
            int basicIndex = -1;
            for (int j = 0; j < coefficients.length; j++) {
                if (coefficients[j][i] == 1 && bValues[j] != 0) {
                    if (!isBasic) {
                        isBasic = true;
                        basicIndex = j;
                    } else {
                        isBasic = false;
                        break;
                    }
                } else if (coefficients[j][i] != 0) {
                    isBasic = false;
                    break;
                }
            }
            if (isBasic) {
                System.out.println("x" + (i + 1) + " = " + (basicIndex < bValues.length - 1 ? bValues[basicIndex] : 0));
            } else {
                System.out.println("x" + (i + 1) + " = 0");
            }
        }
        System.out.println("Значение целевой функции: " + (optimizationGoal == OptimizationGoal.MAXIMIZE ? -bValues[bValues.length - 1] : bValues[bValues.length - 1]));
    }


    public static void main(String[] args) {
        // Пример использования
        double[][] coefficients = {
                {4, 1},
                {1, -1}
        };
        double[] bValues = {8, -3};
        Constraint[] constraints = {Constraint.LESS_THAN_OR_EQUAL_TO_ZERO,
                Constraint.GREATER_THAN_OR_EQUAL_TO_ZERO};

        double[] objectiveFunction = {3, 4};
        OptimizationGoal optimizationGoal = OptimizationGoal.MAXIMIZE;

        SimplexSolver solver = new SimplexSolver(coefficients, bValues, constraints, objectiveFunction, optimizationGoal);
        solver.solve();
    }

}
