package com.Akkshay.expensemanager.controller;

import com.Akkshay.expensemanager.dao.BudgetDAO;
import com.Akkshay.expensemanager.dao.ExpenseDAO;
import com.Akkshay.expensemanager.dao.SavingsDAO;
import com.Akkshay.expensemanager.model.Budget;
import com.Akkshay.expensemanager.model.Expense;
import com.Akkshay.expensemanager.model.Savings;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TrendsController {

    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private Label lblInsight;

    private ExpenseDAO expenseDAO;
    private BudgetDAO budgetDAO;
    private SavingsDAO savingsDAO;

    public TrendsController() {
        this.expenseDAO = new ExpenseDAO();
        this.budgetDAO = new BudgetDAO();
        this.savingsDAO = new SavingsDAO();
    }

    @FXML
    public void initialize() {
        loadChartData();
    }

    private void loadChartData() {
        List<Expense> expenses = expenseDAO.getAllExpenses();
        List<Budget> budgets = budgetDAO.getAllBudgets();
        List<Savings> savings = savingsDAO.getAllSavings();

        // Group by Month (YYYY-MM)
        Map<String, Double> monthlySpend = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> YearMonth.from(e.getExpenseDate()).toString(),
                        Collectors.summingDouble(e -> e.getAmount().doubleValue())));

        Map<String, Double> monthlyBudget = budgets.stream()
                .collect(Collectors.groupingBy(
                        b -> YearMonth.from(b.getBudgetMonth()).toString(),
                        Collectors.summingDouble(b -> b.getAmount().doubleValue())));

        Map<String, Double> monthlySavings = savings.stream()
                .collect(Collectors.groupingBy(
                        s -> YearMonth.from(s.getSavingsDate()).toString(),
                        Collectors.summingDouble(s -> s.getAmount().doubleValue())));

        XYChart.Series<String, Number> spendSeries = new XYChart.Series<>();
        spendSeries.setName("Spend");

        XYChart.Series<String, Number> budgetSeries = new XYChart.Series<>();
        budgetSeries.setName("Budget");

        XYChart.Series<String, Number> savingsSeries = new XYChart.Series<>();
        savingsSeries.setName("Savings");

        // Combine months
        Set<String> allMonths = new TreeSet<>(monthlyBudget.keySet());
        allMonths.addAll(monthlySpend.keySet());
        allMonths.addAll(monthlySavings.keySet());

        for (String month : allMonths) {
            Double budgetVal = monthlyBudget.getOrDefault(month, 0.0);
            Double spendVal = monthlySpend.getOrDefault(month, 0.0);
            Double savingsVal = monthlySavings.getOrDefault(month, 0.0);

            budgetSeries.getData().add(new XYChart.Data<>(month, budgetVal));
            spendSeries.getData().add(new XYChart.Data<>(month, spendVal));
            savingsSeries.getData().add(new XYChart.Data<>(month, savingsVal));
        }

        lineChart.getData().clear();
        lineChart.getData().add(budgetSeries);
        lineChart.getData().add(spendSeries);
        lineChart.getData().add(savingsSeries);

        generateInsights(monthlySpend, monthlyBudget, monthlySavings);
    }

    private void generateInsights(Map<String, Double> spend, Map<String, Double> budget, Map<String, Double> savings) {
        StringBuilder insights = new StringBuilder();

        Set<String> allMonths = new TreeSet<>(budget.keySet());
        allMonths.addAll(spend.keySet());
        allMonths.addAll(savings.keySet());

        for (String month : allMonths) {
            Double budgetVal = budget.getOrDefault(month, 0.0);
            Double spendVal = spend.getOrDefault(month, 0.0);
            Double savingsVal = savings.getOrDefault(month, 0.0);

            // Calculate remaining: Budget - (Spend + Savings)
            double totalUsed = spendVal + savingsVal;
            double remaining = budgetVal - totalUsed;

            if (totalUsed > budgetVal) {
                insights.append("In ").append(month).append(", you overspent by Rs ")
                        .append(String.format("%.2f", totalUsed - budgetVal)).append(".\n");
            } else {
                insights.append("In ").append(month).append(", you were within budget. Remaining: Rs ")
                        .append(String.format("%.2f", remaining))
                        .append(". Total Saved: Rs ").append(String.format("%.2f", savingsVal)).append(".\n");
            }
        }

        if (insights.length() == 0) {
            lblInsight.setText("No data available to generate insights.");
        } else {
            lblInsight.setText(insights.toString());
        }
    }
}
