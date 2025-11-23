package com.Akkshay.expensemanager.controller;

import com.Akkshay.expensemanager.dao.ExpenseDAO;
import com.Akkshay.expensemanager.model.Category;
import com.Akkshay.expensemanager.model.Expense;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseController {

    @FXML
    private TextField txtAmount;
    @FXML
    private DatePicker dpDate;
    @FXML
    private ComboBox<Category> cbCategory;
    @FXML
    private TextField txtDescription;
    @FXML
    private TableView<Expense> tblExpense;
    @FXML
    private TableColumn<Expense, Long> colId;
    @FXML
    private TableColumn<Expense, BigDecimal> colAmount;
    @FXML
    private TableColumn<Expense, LocalDate> colDate;
    @FXML
    private TableColumn<Expense, String> colCategory;
    @FXML
    private TableColumn<Expense, String> colDescription;
    @FXML
    private PieChart pieChart;

    private ExpenseDAO expenseDAO;
    private ObservableList<Expense> expenseList;

    public ExpenseController() {
        this.expenseDAO = new ExpenseDAO();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("expenseId"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("expenseDate"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Configure ComboBox to display category names
        cbCategory.setConverter(new javafx.util.StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getCategoryName() : "";
            }

            @Override
            public Category fromString(String string) {
                // This is called when user types a custom category
                // We'll handle the creation in the add/update methods
                return null;
            }
        });

        loadCategories();
        loadExpenses();
    }

    private void loadCategories() {
        cbCategory.setItems(FXCollections.observableArrayList(expenseDAO.getAllCategories()));
    }

    private void loadExpenses() {
        expenseList = FXCollections.observableArrayList(expenseDAO.getAllExpenses());
        tblExpense.setItems(expenseList);
        updateChart();
    }

    private void updateChart() {
        Map<String, Double> categoryTotals = expenseList.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getCategoryName(),
                        Collectors.summingDouble(e -> e.getAmount().doubleValue())));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        categoryTotals.forEach((name, amount) -> pieData.add(new PieChart.Data(name, amount)));
        pieChart.setData(pieData);
    }

    @FXML
    private void handleAddExpense() {
        try {
            if (txtAmount.getText().isEmpty() || dpDate.getValue() == null) {
                showAlert("Error", "Amount and Date are required.");
                return;
            }
            BigDecimal amount = new BigDecimal(txtAmount.getText());
            LocalDate date = dpDate.getValue();
            Category category = cbCategory.getValue();
            String description = txtDescription.getText();

            if (category == null) {
                // Check if user wants to add a new category
                String newCatName = cbCategory.getEditor().getText();
                if (newCatName != null && !newCatName.trim().isEmpty()) {
                    // Check if it exists
                    category = expenseDAO.getAllCategories().stream()
                            .filter(c -> c.getCategoryName().equalsIgnoreCase(newCatName.trim()))
                            .findFirst()
                            .orElse(null);

                    if (category == null) {
                        // Create and save the new category
                        category = new Category(newCatName.trim());
                        expenseDAO.saveCategory(category);

                        // Reload categories to get the one with ID from database
                        loadCategories();

                        // Retrieve the saved category with its ID
                        category = expenseDAO.getAllCategories().stream()
                                .filter(c -> c.getCategoryName().equalsIgnoreCase(newCatName.trim()))
                                .findFirst()
                                .orElse(null);
                    }
                } else {
                    showAlert("Error", "Please select or enter a category.");
                    return;
                }
            }

            if (category == null) {
                showAlert("Error", "Failed to create or retrieve category.");
                return;
            }

            Expense expense = new Expense(amount, date, description, category);
            expenseDAO.saveExpense(expense);
            loadExpenses();
            clearFields();
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid amount format.");
        } catch (Exception e) {
            showAlert("Error", "Invalid input: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateExpense() {
        Expense selected = tblExpense.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // Only update fields that have values
                if (!txtAmount.getText().isEmpty()) {
                    selected.setAmount(new BigDecimal(txtAmount.getText()));
                }

                if (dpDate.getValue() != null) {
                    selected.setExpenseDate(dpDate.getValue());
                }

                // Update category if changed
                Category category = cbCategory.getValue();
                if (category == null && cbCategory.getEditor().getText() != null
                        && !cbCategory.getEditor().getText().trim().isEmpty()) {
                    String newCatName = cbCategory.getEditor().getText();
                    category = expenseDAO.getAllCategories().stream()
                            .filter(c -> c.getCategoryName().equalsIgnoreCase(newCatName.trim()))
                            .findFirst()
                            .orElse(null);
                    if (category == null) {
                        // Create and save the new category
                        category = new Category(newCatName.trim());
                        expenseDAO.saveCategory(category);

                        // Reload categories to get the one with ID from database
                        loadCategories();

                        // Retrieve the saved category with its ID
                        category = expenseDAO.getAllCategories().stream()
                                .filter(c -> c.getCategoryName().equalsIgnoreCase(newCatName.trim()))
                                .findFirst()
                                .orElse(null);
                    }
                }
                if (category != null) {
                    selected.setCategory(category);
                }

                // Update description if not empty
                if (!txtDescription.getText().isEmpty()) {
                    selected.setDescription(txtDescription.getText());
                }

                expenseDAO.updateExpense(selected);
                loadExpenses();
                clearFields();
                showAlert("Success", "Expense updated successfully!");
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid amount format.");
            } catch (Exception e) {
                showAlert("Error", "Invalid input: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Warning", "Please select an expense to update.");
        }
    }

    @FXML
    private void handleDeleteExpense() {
        Expense selected = tblExpense.getSelectionModel().getSelectedItem();
        if (selected != null) {
            expenseDAO.deleteExpense(selected);
            loadExpenses();
        } else {
            showAlert("Warning", "Please select an expense to delete.");
        }
    }

    private void clearFields() {
        txtAmount.clear();
        dpDate.setValue(null);
        cbCategory.getSelectionModel().clearSelection();
        txtDescription.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
