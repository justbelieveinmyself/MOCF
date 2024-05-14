package com.justbelieveinmyself.mocf;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class MainController {

    @FXML
    private TextField variablesCountField;

    @FXML
    private TextField constraintsCountField;

    @FXML
    private GridPane tableGridPane;

    @FXML
    private GridPane constraintsGridPane;

    @FXML
    private GridPane targetFunctionGridPane;

    @FXML
    private Button calculateButton;

    private int variablesCount;
    private int constraintsCount;

    @FXML
    private void onNextButtonClick() {
        variablesCount = Integer.parseInt(variablesCountField.getText());
        constraintsCount = Integer.parseInt(constraintsCountField.getText());

        tableGridPane.getChildren().clear();
        constraintsGridPane.getChildren().clear();
        targetFunctionGridPane.getChildren().clear();

        for (int col = 0; col < variablesCount; col++) {
            String columnName = "x" + (col + 1);
            Label label = new Label(columnName);
            tableGridPane.add(label, col, 0);
        }
        Label labelSign = new Label("Знак");
        tableGridPane.add(labelSign, variablesCount, 0);
        Label labelB = new Label("B");
        tableGridPane.add(labelB, variablesCount + 1, 0);

        for (int row = 1; row <= constraintsCount; row++) {
            for (int col = 0; col < variablesCount; col++) {
                TextField textField = new TextField();
                textField.setText("0");
                tableGridPane.add(textField, col, row);
            }
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.getItems().addAll(">=", "=", "<=");
            comboBox.setValue(">=");
            tableGridPane.add(comboBox, variablesCount, row);

            TextField bField = new TextField();
            bField.setText("0");
            tableGridPane.add(bField, variablesCount + 1, row);
        }

        for (int i = 0; i < variablesCount; i++) {
            Label label = new Label("x" + (i + 1) + ":");
            constraintsGridPane.add(label, 0, i);

            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.getItems().addAll(">= 0", "Нет", "<= 0");
            comboBox.setValue(">= 0");
            constraintsGridPane.add(comboBox, 1, i);
        }

        for (int col = 0; col < variablesCount + 1; col++) {
            String columnName;
            if (col < variablesCount) {
                columnName = "x" + (col + 1);
            } else {
                columnName = "C";
            }
            Label label = new Label(columnName);
            targetFunctionGridPane.add(label, col, 0);
        }

        for (int col = 0; col < variablesCount + 1; col++) {
            TextField textField = new TextField();
            textField.setText("0");
            targetFunctionGridPane.add(textField, col, 1);
        }

        Label label = new Label("Цель:");
        constraintsGridPane.add(label, 0, variablesCount);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Минимизация", "Максимизация");
        comboBox.setValue("Минимизация");
        constraintsGridPane.add(comboBox, 1, variablesCount);

        calculateButton.setVisible(true);
    }

    @FXML
    private void onCalculateButtonClick() {
        double[][] coefficients = new double[constraintsCount][variablesCount];
        double[] bValues = new double[constraintsCount];
        Constraint[] constraints = new Constraint[constraintsCount];
        double[] objectiveFunction = new double[variablesCount];
        OptimizationGoal optimizationGoal;

        for (int row = 1; row <= constraintsCount; row++) {
            for (int col = 0; col < variablesCount; col++) {
                TextField textField = (TextField) tableGridPane.getChildren().get(row * (variablesCount + 2) + col);
                coefficients[row - 1][col] = Double.parseDouble(textField.getText());
            }
            ComboBox<String> comboBox = (ComboBox<String>) tableGridPane.getChildren().get(row * (variablesCount + 2) + variablesCount);
            String sign = comboBox.getValue();
            switch (sign) {
                case ">=":
                    constraints[row - 1] = Constraint.GREATER_THAN_OR_EQUAL_TO_ZERO;
                    break;
                case "<=":
                    constraints[row - 1] = Constraint.LESS_THAN_OR_EQUAL_TO_ZERO;
                    break;
                default:
                    constraints[row - 1] = Constraint.NO_CONSTRAINTS;
                    break;
            }
            TextField bField = (TextField) tableGridPane.getChildren().get(row * (variablesCount + 2) + variablesCount + 1);
            bValues[row - 1] = Double.parseDouble(bField.getText());
        }

        for (Node node : targetFunctionGridPane.getChildren()) {
            if (node instanceof TextField) {
                Integer colIndex = GridPane.getColumnIndex(node);
                if (colIndex != null && colIndex < variablesCount) {
                    objectiveFunction[colIndex] = Double.parseDouble(((TextField) node).getText());
                }
            }
        }



        Node node = constraintsGridPane.getChildren().get(variablesCount + 1);
        String goal = "";
        if (node instanceof ComboBox) {
            ComboBox<String> comboBox = (ComboBox<String>) node;
            goal = comboBox.getValue();
        }

        if (goal.equals("Минимизация")) {
            optimizationGoal = OptimizationGoal.MINIMIZE;
        } else {
            optimizationGoal = OptimizationGoal.MAXIMIZE;
        }

//        SimplexSolver solver = new SimplexSolver(coefficients, bValues, constraints, objectiveFunction, optimizationGoal);

//        double[] solution = solver.solve();

/*        if (solution == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Задача не ограничена");
            alert.showAndWait();
        } else {
            StringBuilder result = new StringBuilder("Оптимальные значения переменных:\n");
            for (int i = 0; i < solution.length; i++) {
                result.append("x").append(i + 1).append(": ").append(solution[i]).append("\n");
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Результаты решения");
            alert.setHeaderText(null);
            alert.setContentText(result.toString());
            alert.showAndWait();
        }*/
    }



}
