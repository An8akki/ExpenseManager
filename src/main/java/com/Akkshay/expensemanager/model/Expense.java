package com.Akkshay.expensemanager.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents the EXPENSES table in the database.
 * Each instance of this class is a single expense row.
 */
@Entity
@Table(name = "TBL_EXPENSES")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long expenseId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "description", length = 255)
    private String description;

    // Establishes a many-to-one relationship with Category.
    // Many expenses can belong to one category.
    @ManyToOne(fetch = FetchType.EAGER) // EAGER fetch is fine here as we always show the category with the expense.
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // --- Constructors --- //

    /**
     * Default constructor required by Hibernate.
     */
    public Expense() {
    }

    public Expense(BigDecimal amount, LocalDate expenseDate, String description, Category category) {
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.description = description;
        this.category = category;
    }

    // --- Getters and Setters --- //

    public Long getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Long expenseId) {
        this.expenseId = expenseId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}

