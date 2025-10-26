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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseManagerController {

    // --- FXML UI Components --- //
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private Label statusLabel;
    @FXML private TableView<Expense> expenseTable;
    @FXML private TableColumn<Expense, String> descriptionColumn;
    @FXML private TableColumn<Expense, BigDecimal> amountColumn;
    @FXML private TableColumn<Expense, LocalDate> dateColumn;
    @FXML private TableColumn<Expense, Category> categoryColumn;
    @FXML private PieChart expensePieChart;

    // --- Backend and Data --- //
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final ObservableList<Expense> expenseList = FXCollections.observableArrayList();
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();

    /**
     * This method is automatically called after the fxml file has been loaded.
     * It's used to initialize the UI components.
     */
    @FXML
    public void initialize() {
        // 1. Setup Table Columns
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("expenseDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // 2. Load data from the database
        loadCategories();
        loadExpenses();

        // 3. Set the items for the table and combobox
        expenseTable.setItems(expenseList);
        categoryComboBox.setItems(categoryList);

        // 4. Set default date to today
        datePicker.setValue(LocalDate.now());

        // 5. Update the pie chart
        updatePieChart();
    }

    /**
     * Handles the "Add Expense" button click.
     * Validates input and saves the new expense.
     */
    @FXML
    private void handleAddExpense() {
        // --- Input Validation --- //
        if (descriptionField.getText().isEmpty() || amountField.getText().isEmpty() || datePicker.getValue() == null || categoryComboBox.getValue() == null) {
            statusLabel.setText("All fields are required.");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                statusLabel.setText("Amount must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid amount format.");
            return;
        }

        // --- Create and Save Expense --- //
        Expense newExpense = new Expense(
                amount,
                datePicker.getValue(),
                descriptionField.getText(),
                categoryComboBox.getValue()
        );

        expenseDAO.saveExpense(newExpense);
        loadExpenses(); // Reload expenses to show the new one
        updatePieChart(); // Update chart
        clearForm();
        statusLabel.setText("Expense added successfully!");
    }

    /**
     * Handles the "Delete Selected" button click.
     * Removes the selected expense from the table and database.
     */
    @FXML
    private void handleDeleteExpense() {
        Expense selectedExpense = expenseTable.getSelectionModel().getSelectedItem();
        if (selectedExpense == null) {
            statusLabel.setText("Please select an expense to delete.");
            return;
        }

        expenseDAO.deleteExpense(selectedExpense);
        loadExpenses(); // Reload to reflect deletion
        updatePieChart(); // Update chart
        statusLabel.setText("Expense deleted successfully.");
    }

    /**
     * Loads categories from the database and populates the category list.
     */
    private void loadCategories() {
        // For simplicity, let's add some default categories if none exist
        List<Category> categories = expenseDAO.getAllCategories();
        if (categories.isEmpty()) {
            expenseDAO.saveCategory(new Category("Food"));
            expenseDAO.saveCategory(new Category("Travel"));
            expenseDAO.saveCategory(new Category("Bills"));
            expenseDAO.saveCategory(new Category("Entertainment"));
            expenseDAO.saveCategory(new Category("Other"));
            categories = expenseDAO.getAllCategories();
        }
        categoryList.setAll(categories);
    }

    /**
     * Loads all expenses from the database into the observable list.
     */
    private void loadExpenses() {
        List<Expense> expenses = expenseDAO.getAllExpenses();
        expenseList.setAll(expenses);
    }

    /**
     * Updates the pie chart with the current expense data.
     */
    private void updatePieChart() {
        Map<String, Double> expenseByCategory = expenseList.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getCategory().getCategoryName(),
                        Collectors.summingDouble(expense -> expense.getAmount().doubleValue())
                ));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        expenseByCategory.forEach((category, total) ->
                pieChartData.add(new PieChart.Data(category, total))
        );

        expensePieChart.setData(pieChartData);
    }

    /**
     * Clears the input form fields.
     */
    private void clearForm() {
        descriptionField.clear();
        amountField.clear();
        datePicker.setValue(LocalDate.now());
        categoryComboBox.getSelectionModel().clearSelection();
    }
}
