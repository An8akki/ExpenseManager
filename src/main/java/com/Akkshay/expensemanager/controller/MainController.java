package com.Akkshay.expensemanager.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.net.URL;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // Load Home view by default
        loadView("/com/Akkshay/expensemanager/view/Home.fxml");
    }

    @FXML
    private void handleShowHome() {
        loadView("/com/Akkshay/expensemanager/view/Home.fxml");
    }

    @FXML
    private void handleShowBudget() {
        loadView("/com/Akkshay/expensemanager/view/Budget.fxml");
    }

    @FXML
    private void handleShowExpense() {
        // Assuming the existing Dashboard.fxml is the Expense view, or we create a new
        // one.
        // Let's assume we'll create Expense.fxml or reuse Dashboard.fxml
        loadView("/com/Akkshay/expensemanager/view/Expense.fxml");
    }

    @FXML
    private void handleShowSavings() {
        loadView("/com/Akkshay/expensemanager/view/Savings.fxml");
    }

    @FXML
    private void handleShowTrends() {
        loadView("/com/Akkshay/expensemanager/view/Trends.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Cannot find FXML: " + fxmlPath);
                return;
            }
            Parent view = FXMLLoader.load(resource);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
