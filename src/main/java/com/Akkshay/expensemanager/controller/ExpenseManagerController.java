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

    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final ObservableList<Expense> expenseList = FXCollections.observableArrayList();
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();

    private Expense selectedExpense = null;

    @FXML
    public void initialize() {
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("expenseDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        loadCategories();
        loadExpenses();

        expenseTable.setItems(expenseList);
        categoryComboBox.setItems(categoryList);
        categoryComboBox.setEditable(true);

        datePicker.setValue(LocalDate.now());
        updatePieChart();

        // Row click for update functionality
        expenseTable.setRowFactory(tv -> {
            TableRow<Expense> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    selectedExpense = row.getItem();
                    descriptionField.setText(selectedExpense.getDescription());
                    amountField.setText(selectedExpense.getAmount().toString());
                    datePicker.setValue(selectedExpense.getExpenseDate());
                    categoryComboBox.setValue(selectedExpense.getCategory());
                }
            });
            return row;
        });
    }

    @FXML
    private void handleAddExpense() {
        if (descriptionField.getText().isEmpty() ||
                amountField.getText().isEmpty() ||
                datePicker.getValue() == null ||
                (categoryComboBox.getValue() == null && categoryComboBox.getEditor().getText().trim().isEmpty())) {
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

        // --- Robust category logic for ComboBox (typed or selected) ---
        Object catObj = categoryComboBox.getValue();
        Category category;
        if (catObj instanceof Category) {
            category = (Category) catObj;
        } else if (catObj instanceof String) {
            String catText = ((String) catObj).trim();
            if (!catText.isEmpty()) {
                category = new Category(catText);
                expenseDAO.saveCategory(category);    // store new category!
                loadCategories();                     // refresh dropdown
                // Now select the correct added Category object
                for (Category c : categoryList) {
                    if (c.getCategoryName().equals(catText)) {
                        category = c;
                        break;
                    }
                }
                categoryComboBox.setValue(category);
            } else {
                statusLabel.setText("Category is required.");
                return;
            }
        } else {
            statusLabel.setText("Category is required.");
            return;
        }
        updateRecentCategory(category);

        if (selectedExpense == null) {
            // Add new Expense
            Expense newExpense = new Expense(
                    amount,
                    datePicker.getValue(),
                    descriptionField.getText(),
                    category
            );
            expenseDAO.saveExpense(newExpense);
            statusLabel.setText("Expense added successfully!");
        } else {
            // Update existing Expense
            selectedExpense.setDescription(descriptionField.getText());
            selectedExpense.setAmount(amount);
            selectedExpense.setExpenseDate(datePicker.getValue());
            selectedExpense.setCategory(category);
            expenseDAO.updateExpense(selectedExpense);
            statusLabel.setText("Expense updated successfully!");
            selectedExpense = null;
        }
        loadExpenses();
        updatePieChart();
        clearForm();
    }

    @FXML
    private void handleDeleteExpense() {
        Expense exp = expenseTable.getSelectionModel().getSelectedItem();
        if (exp == null) {
            statusLabel.setText("Please select an expense to delete.");
            return;
        }
        expenseDAO.deleteExpense(exp);
        loadExpenses();
        updatePieChart();
        statusLabel.setText("Expense deleted successfully.");
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        selectedExpense = null;
        expenseTable.getSelectionModel().clearSelection();
        statusLabel.setText(""); // Clear status label if you want
    }

    private void loadCategories() {
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

    private void loadExpenses() {
        expenseList.setAll(expenseDAO.getAllExpenses());
    }

    private void updatePieChart() {
        Map<String, Double> expenseByCategory = expenseList.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getCategoryName(),
                        Collectors.summingDouble(e -> e.getAmount().doubleValue())
                ));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        expenseByCategory.forEach((category, total) ->
                pieChartData.add(new PieChart.Data(category, total))
        );
        expensePieChart.setData(pieChartData);
    }

    private void clearForm() {
        descriptionField.clear();
        amountField.clear();
        datePicker.setValue(LocalDate.now());
        categoryComboBox.getSelectionModel().clearSelection();
        categoryComboBox.getEditor().clear();
    }

    // Recent category helper (optionally limits list to last N, e.g., 10)
    private void updateRecentCategory(Category category) {
        categoryList.remove(category);
        categoryList.add(0, category);
        if (categoryList.size() > 10) {
            categoryList.remove(10, categoryList.size());
        }
        categoryComboBox.setItems(categoryList);
    }
}
