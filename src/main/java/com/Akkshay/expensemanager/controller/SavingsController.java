package com.Akkshay.expensemanager.controller;

import com.Akkshay.expensemanager.dao.SavingsDAO;
import com.Akkshay.expensemanager.model.Savings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingsController {

    @FXML
    private TextField txtAmount;
    @FXML
    private DatePicker dpDate;
    @FXML
    private TextField txtGoal;
    @FXML
    private TextField txtDescription;
    @FXML
    private TableView<Savings> tblSavings;
    @FXML
    private TableColumn<Savings, Long> colId;
    @FXML
    private TableColumn<Savings, BigDecimal> colAmount;
    @FXML
    private TableColumn<Savings, LocalDate> colDate;
    @FXML
    private TableColumn<Savings, String> colGoal;
    @FXML
    private TableColumn<Savings, String> colDescription;
    @FXML
    private Label lblTotalSavings;
    @FXML
    private Label lblSavingsCount;
    @FXML
    private Label lblActiveGoals;

    private SavingsDAO savingsDAO;
    private ObservableList<Savings> savingsList;

    public SavingsController() {
        this.savingsDAO = new SavingsDAO();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("savingsId"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("savingsDate"));
        colGoal.setCellValueFactory(new PropertyValueFactory<>("goal"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        loadSavings();
    }

    private void loadSavings() {
        savingsList = FXCollections.observableArrayList(savingsDAO.getAllSavings());
        tblSavings.setItems(savingsList);
        updateSummary();
    }

    private void updateSummary() {
        BigDecimal total = savingsList.stream()
                .map(Savings::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotalSavings.setText("Rs " + total.toString());
        lblSavingsCount.setText(savingsList.size() + " savings entries");

        // Count unique goals
        long uniqueGoals = savingsList.stream()
                .map(Savings::getGoal)
                .filter(goal -> goal != null && !goal.trim().isEmpty())
                .distinct()
                .count();

        lblActiveGoals.setText(String.valueOf(uniqueGoals));
    }

    @FXML
    private void handleAddSavings() {
        try {
            if (txtAmount.getText().isEmpty() || dpDate.getValue() == null) {
                showAlert("Error", "Amount and Date are required.");
                return;
            }
            BigDecimal amount = new BigDecimal(txtAmount.getText());
            LocalDate date = dpDate.getValue();
            String goal = txtGoal.getText();
            String description = txtDescription.getText();

            Savings savings = new Savings(amount, date, description, goal);
            savingsDAO.saveSavings(savings);
            loadSavings();
            clearFields();
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid amount format.");
        } catch (Exception e) {
            showAlert("Error", "Invalid input: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateSavings() {
        Savings selected = tblSavings.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // Only update fields that have values
                if (!txtAmount.getText().isEmpty()) {
                    selected.setAmount(new BigDecimal(txtAmount.getText()));
                }

                if (dpDate.getValue() != null) {
                    selected.setSavingsDate(dpDate.getValue());
                }

                if (!txtGoal.getText().isEmpty()) {
                    selected.setGoal(txtGoal.getText());
                }

                if (!txtDescription.getText().isEmpty()) {
                    selected.setDescription(txtDescription.getText());
                }

                savingsDAO.updateSavings(selected);
                loadSavings();
                clearFields();
                showAlert("Success", "Savings updated successfully!");
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid amount format.");
            } catch (Exception e) {
                showAlert("Error", "Invalid input: " + e.getMessage());
            }
        } else {
            showAlert("Warning", "Please select a savings entry to update.");
        }
    }

    @FXML
    private void handleDeleteSavings() {
        Savings selected = tblSavings.getSelectionModel().getSelectedItem();
        if (selected != null) {
            savingsDAO.deleteSavings(selected);
            loadSavings();
        } else {
            showAlert("Warning", "Please select a savings entry to delete.");
        }
    }

    private void clearFields() {
        txtAmount.clear();
        dpDate.setValue(null);
        txtGoal.clear();
        txtDescription.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
