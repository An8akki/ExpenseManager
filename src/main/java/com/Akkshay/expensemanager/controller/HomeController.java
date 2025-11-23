package com.Akkshay.expensemanager.controller;

import com.Akkshay.expensemanager.dao.BudgetDAO;
import com.Akkshay.expensemanager.dao.ExpenseDAO;
import com.Akkshay.expensemanager.dao.SavingsDAO;
import com.Akkshay.expensemanager.model.Budget;
import com.Akkshay.expensemanager.model.Expense;
import com.Akkshay.expensemanager.model.Savings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.util.List;

public class HomeController {

    @FXML
    private Label lblTotalSpend;
    @FXML
    private Label lblTotalSavings;
    @FXML
    private Label lblTotalBudget;
    @FXML
    private Label lblComment;

    private ExpenseDAO expenseDAO;
    private BudgetDAO budgetDAO;
    private SavingsDAO savingsDAO;

    public HomeController() {
        this.expenseDAO = new ExpenseDAO();
        this.budgetDAO = new BudgetDAO();
        this.savingsDAO = new SavingsDAO();
    }

    @FXML
    public void initialize() {
        refreshData();
    }

    public void refreshData() {
        updateDashboard();
    }

    private BigDecimal calculateTotalSpend() {
        List<Expense> expenses = expenseDAO.getAllExpenses();
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalBudget() {
        List<Budget> budgets = budgetDAO.getAllBudgets();
        return budgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalSavings() {
        List<Savings> savings = savingsDAO.getAllSavings();
        return savings.stream()
                .map(Savings::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void updateDashboard() {
        BigDecimal totalSpend = calculateTotalSpend();
        BigDecimal totalBudget = calculateTotalBudget();
        BigDecimal totalSavings = calculateTotalSavings();

        lblTotalSpend.setText("Rs " + totalSpend.toString());
        lblTotalBudget.setText("Rs " + totalBudget.toString());
        lblTotalSavings.setText("Rs " + totalSavings.toString());

        updateComment(totalSpend, totalBudget, totalSavings);
    }

    private void updateComment(BigDecimal spend, BigDecimal budget, BigDecimal savings) {
        BigDecimal remaining = budget.subtract(spend).subtract(savings);

        StringBuilder sb = new StringBuilder();
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            sb.append("Warning: You have exceeded your total budget by Rs ").append(remaining.abs());
            lblComment.setStyle("-fx-text-fill: red;");
        } else {
            sb.append("Good job! You have Rs ").append(remaining).append(" remaining after expenses and savings.");
            lblComment.setStyle("-fx-text-fill: green;");
        }

        if (savings.compareTo(BigDecimal.ZERO) > 0) {
            sb.append("\nGreat! You have saved Rs ").append(savings).append(".");
        } else {
            sb.append("\nConsider setting aside some savings.");
        }

        lblComment.setText(sb.toString());
    }
}
