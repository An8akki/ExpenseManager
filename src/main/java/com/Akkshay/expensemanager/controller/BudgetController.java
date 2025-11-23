package com.Akkshay.expensemanager.controller;

import com.Akkshay.expensemanager.dao.BudgetDAO;
import com.Akkshay.expensemanager.model.Budget;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BudgetController {

    @FXML
    private TextField txtAmount;
    @FXML
    private DatePicker dpMonth;
    @FXML
    private TextField txtDescription;
    @FXML
    private TableView<Budget> tblBudget;
    @FXML
    private TableColumn<Budget, Long> colId;
    @FXML
    private TableColumn<Budget, BigDecimal> colAmount;
    @FXML
    private TableColumn<Budget, LocalDate> colMonth;
    @FXML
    private TableColumn<Budget, String> colDescription;
    @FXML
    private Label lblTotalBudget;
    @FXML
    private Label lblBudgetCount;
    @FXML
    private Label lblCurrentMonthBudget;
    @FXML
    private Label lblCurrentMonth;

    private BudgetDAO budgetDAO;
    private ObservableList<Budget> budgetList;

    public BudgetController() {
        this.budgetDAO = new BudgetDAO();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("budgetId"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colMonth.setCellValueFactory(new PropertyValueFactory<>("budgetMonth"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        loadBudgets();
    }

    private void loadBudgets() {
        budgetList = FXCollections.observableArrayList(budgetDAO.getAllBudgets());
        tblBudget.setItems(budgetList);
        updateSummary();
    }

    private void updateSummary() {
        BigDecimal total = budgetList.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotalBudget.setText("Rs " + total.toString());
        lblBudgetCount.setText(budgetList.size() + " budgets planned");

        // Current month budget
        LocalDate now = LocalDate.now();
        LocalDate currentMonth = now.withDayOfMonth(1);
        BigDecimal currentMonthBudget = budgetList.stream()
                .filter(b -> b.getBudgetMonth().equals(currentMonth))
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblCurrentMonthBudget.setText("Rs " + currentMonthBudget.toString());
        lblCurrentMonth.setText(now.getMonth().toString() + " " + now.getYear());
    }

    @FXML
    private void handleAddBudget() {
        try {
            if (txtAmount.getText().isEmpty() || dpMonth.getValue() == null) {
                showAlert("Error", "Amount and Month are required.");
                return;
            }
            BigDecimal amount = new BigDecimal(txtAmount.getText());
            LocalDate month = dpMonth.getValue();
            // Ensure month is set to the 1st
            month = month.withDayOfMonth(1);
            String description = txtDescription.getText();

            Budget budget = new Budget(amount, month, description);
            budgetDAO.saveBudget(budget);
            loadBudgets();
            clearFields();
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid amount format.");
        } catch (Exception e) {
            showAlert("Error", "Invalid input: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateBudget() {
        Budget selected = tblBudget.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // Only update fields that have values
                if (!txtAmount.getText().isEmpty()) {
                    selected.setAmount(new BigDecimal(txtAmount.getText()));
                }

                if (dpMonth.getValue() != null) {
                    selected.setBudgetMonth(dpMonth.getValue().withDayOfMonth(1));
                }

                if (!txtDescription.getText().isEmpty()) {
                    selected.setDescription(txtDescription.getText());
                }

                budgetDAO.updateBudget(selected);
                loadBudgets();
                clearFields();
                showAlert("Success", "Budget updated successfully!");
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid amount format.");
            } catch (Exception e) {
                showAlert("Error", "Invalid input: " + e.getMessage());
            }
        } else {
            showAlert("Warning", "Please select a budget to update.");
        }
    }

    @FXML
    private void handleDeleteBudget() {
        Budget selected = tblBudget.getSelectionModel().getSelectedItem();
        if (selected != null) {
            budgetDAO.deleteBudget(selected);
            loadBudgets();
        } else {
            showAlert("Warning", "Please select a budget to delete.");
        }
    }

    private void clearFields() {
        txtAmount.clear();
        dpMonth.setValue(null);
        txtDescription.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
