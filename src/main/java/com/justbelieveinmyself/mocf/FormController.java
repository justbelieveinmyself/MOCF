package com.justbelieveinmyself.mocf;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;

import java.util.List;
import java.util.function.Function;

public class FormController {
    @FXML
    private TextField constraintsInput;
    @FXML
    private TextField variablesInput;
    @FXML
    private TableView<String[]> table;
    @FXML
    public TextArea resultTextArea;
    @FXML
    public ComboBox<String> methodComboBox;

    public void createTable() {
        int constraints = Integer.parseInt(constraintsInput.getText());
        int variables = Integer.parseInt(variablesInput.getText());

        table.getColumns().clear();

        // Создание столбцов
        for (int i = 0; i <= variables + 1; i++) {
            TableColumn<String[], String> column = new TableColumn<>();
            if (i == 0) {
                column.setText("Базис");
            } else if (i <= variables) {
                column.setText("X" + i);
            } else {
                column.setText("Решение");
            }
            final int columnIndex = i;
            column.setCellValueFactory(cellData -> {
                String[] row = cellData.getValue();
                return row != null && row.length > columnIndex ?
                        new SimpleStringProperty(row[columnIndex]) :
                        new SimpleStringProperty("");
            });

            // Разрешаем редактирование всех ячеек
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setOnEditCommit(event -> {
                String[] row = event.getRowValue();
                row[columnIndex] = event.getNewValue();
            });

            table.getColumns().add(column);
        }

        // Создание строк
        table.getItems().clear();
        for (int i = 0; i < constraints; i++) {
            String[] row = new String[variables + 2];
            row[0] = "x" + (i + 3);
            table.getItems().add(row);
        }
        String[] row = new String[variables + 2];
        row[0] = "E";
        table.getItems().add(row);

        // Разрешаем редактирование всех строк
        table.setEditable(true);

        // Очистка таблицы
        table.refresh();
    }


    public void solveSimplex() {
        ObservableList<String[]> data = table.getItems();

        int numRows = data.size();
        int numCols = data.get(0).length;

        double[][] tableau = new double[numRows][numCols];

        // Заполнение симплекс-таблицы данными из таблицы JavaFX
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                try {
                    tableau[i][j] = Double.parseDouble(data.get(i)[j]);
                } catch (NumberFormatException e) {
                    tableau[i][j] = 0.0;
                }
            }
        }

        // Преобразование симплекс-таблицы
        boolean continueIteration = true;
        while (continueIteration) {
            // Поиск ведущего столбца
            int pivotColumn = -1;
            double minCoefficient = Double.MAX_VALUE;
            for (int j = 1; j < numCols - 1; j++) {
                if (tableau[numRows - 1][j] < 0 && tableau[numRows - 1][j] < minCoefficient) {
                    minCoefficient = tableau[numRows - 1][j];
                    pivotColumn = j;
                }
            }

            if (pivotColumn == -1) {
                continueIteration = false; // Оптимальное решение достигнуто
            } else {
                // Поиск ведущей строки
                int pivotRow = -1;
                double minRatio = Double.MAX_VALUE;
                for (int i = 0; i < numRows - 1; i++) {
                    if (tableau[i][pivotColumn] > 0) {
                        double ratio = tableau[i][numCols - 1] / tableau[i][pivotColumn];
                        if (ratio < minRatio) {
                            minRatio = ratio;
                            pivotRow = i;
                        }
                    }
                }

                if (pivotRow == -1) {
                    continueIteration = false; // Неограниченная целевая функция
                } else {
                    // Обновление симплекс-таблицы
                    double pivotElement = tableau[pivotRow][pivotColumn];
                    for (int i = 0; i < numRows; i++) {
                        for (int j = 0; j < numCols; j++) {
                            if (i != pivotRow && j != pivotColumn) {
                                tableau[i][j] -= (tableau[pivotRow][j] * tableau[i][pivotColumn]) / pivotElement;
                            }
                        }
                    }
                    for (int j = 0; j < numCols; j++) {
                        if (j != pivotColumn) {
                            tableau[pivotRow][j] /= pivotElement;
                        }
                    }
                    for (int i = 0; i < numRows; i++) {
                        if (i != pivotRow) {
                            tableau[i][pivotColumn] /= -pivotElement;
                        }
                    }
                    tableau[pivotRow][pivotColumn] = 1 / pivotElement;
                }
            }
        }

        // Обновление таблицы с результатами
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                data.get(i)[j] = String.valueOf(tableau[i][j]);
            }
        }
        table.refresh();
    }

    @FXML
    public void calculateMinimum(ActionEvent event) {
        String selectedMethod = methodComboBox.getValue();

        Function<List<Double>, Double> functionToMinimize;
        List<List<Double>> startingPoints;
        if (selectedMethod.equals("Функция Розенброка двумерная")) {
            functionToMinimize = Gradient.rosenbrock;
            startingPoints = Gradient.points2D;
        } else if (selectedMethod.equals("Функция Розенброка трёхмерная")) {
            functionToMinimize = Gradient.rosenbrock3D;
            startingPoints = Gradient.points3D;
        } else if (selectedMethod.equals("Функция Растригина двумерная")) {
            functionToMinimize = Gradient.rastrigin;
            startingPoints = Gradient.points2D;
        } else {
            functionToMinimize = Gradient.rastrigin;
            startingPoints = Gradient.points3D;
        }

        // Выполняем минимизацию
        List<List<Double>> localMinima = Gradient.minimize(
                functionToMinimize,
                startingPoints,
                0.0001, // learningRate
                1e-10 // tolerance
        );

        resultTextArea.appendText("Локальные минимумы для " + selectedMethod + ":\n");
        for (int i = 0; i < localMinima.size(); i++) {
            resultTextArea.appendText("Min " + (i + 1) + ": (");
            for (int j = 0; j < localMinima.get(i).size(); j++) {
                if (j == 0) resultTextArea.appendText(localMinima.get(i).get(j).toString());
                else resultTextArea.appendText(", " + localMinima.get(i).get(j).toString());
            }
            resultTextArea.appendText(")\n");
        }
    }
}
